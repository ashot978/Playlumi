#!/usr/bin/env python3
"""Sign Playlumi release APK privately with stable PKCS12 key, APK v1 and v2.
No key material is stored or passed through the repository.
Usage: python tools/sign-release.py unsigned.apk private.p12 password.txt release.apk
Requires cryptography, keytool/jarsigner from JDK.
"""
import sys,struct,hashlib,subprocess,tempfile,zipfile
from pathlib import Path
from cryptography.hazmat.primitives.serialization import pkcs12, Encoding, PublicFormat
from cryptography.hazmat.primitives.asymmetric import padding, rsa
from cryptography.hazmat.primitives import hashes

ALG=0x0103
V2=0x7109871a
MAGIC=b'APK Sig Block 42'
def u32(n):return struct.pack('<I',n)
def u64(n):return struct.pack('<Q',n)
def prefixed(b):return u32(len(b))+b

def offsets(apk):
    eocd=apk.rfind(b'PK\x05\x06')
    if eocd<0 or eocd+22>len(apk):raise ValueError('invalid APK ZIP EOCD')
    if int.from_bytes(apk[eocd+20:eocd+22],'little')!=len(apk)-eocd-22:
        raise ValueError('ZIP trailing data is not supported')
    central=struct.unpack_from('<I',apk,eocd+16)[0]
    if central>=eocd or central==0xffffffff:raise ValueError('invalid/ZIP64 APK')
    return central,eocd

def chunk_digest(parts):
    digests=[]
    for part in parts:
        for start in range(0,len(part),1024*1024):
            chunk=part[start:start+1024*1024]
            digests.append(hashlib.sha256(b'\xa5'+u32(len(chunk))+chunk).digest())
    return hashlib.sha256(b'\x5a'+u32(len(digests))+b''.join(digests)).digest()

def v2_sign(data,key,cert):
    central,eocd=offsets(data)
    digest=chunk_digest((data[:central],data[central:eocd],data[eocd:]))
    signed_data=(prefixed(prefixed(u32(ALG)+prefixed(digest)))+
                 prefixed(prefixed(cert.public_bytes(Encoding.DER)))+prefixed(b''))
    signature=key.sign(signed_data,padding.PKCS1v15(),hashes.SHA256())
    sig_records=prefixed(prefixed(u32(ALG)+prefixed(signature)))
    public=key.public_key().public_bytes(Encoding.DER,PublicFormat.SubjectPublicKeyInfo)
    signer=prefixed(signed_data)+sig_records+prefixed(public)
    pair=u64(len(prefixed(prefixed(signer)))+4)+u32(V2)+prefixed(prefixed(signer))
    size=len(pair)+8+len(MAGIC)
    block=u64(size)+pair+u64(size)+MAGIC
    final_eocd=bytearray(data[eocd:])
    struct.pack_into('<I',final_eocd,16,central+len(block))
    return data[:central]+block+data[central:eocd]+final_eocd

def sign(unsigned,keystore,passwordfile,output):
    password=Path(passwordfile).read_text().strip()
    key,cert,_=pkcs12.load_key_and_certificates(Path(keystore).read_bytes(),password.encode())
    if not isinstance(key,rsa.RSAPrivateKey) or cert is None:
        raise ValueError('Expected RSA certificate and private key')
    with tempfile.TemporaryDirectory() as tmp:
        v1=Path(tmp)/'v1.apk'
        subprocess.run(['jarsigner','-keystore',str(keystore),'-storetype','PKCS12',
            '-storepass',password,'-keypass',password,'-sigalg','SHA256withRSA',
            '-digestalg','SHA-256','-signedjar',str(v1),str(unsigned),'playlumi-release'],
            check=True,stdout=subprocess.DEVNULL)
        final=v2_sign(v1.read_bytes(),key,cert)
        Path(output).write_bytes(final)
    with zipfile.ZipFile(output) as apk:
        assert apk.testzip() is None, 'Corrupted ZIP'
        assert 'META-INF/MANIFEST.MF' in apk.namelist()
    central,_=offsets(final)
    assert final[central-16:central]==MAGIC, 'v2 signing block missing'
    subprocess.run(['jarsigner','-verify',str(output)],check=True,
                   stdout=subprocess.DEVNULL,stderr=subprocess.DEVNULL)
    print('Signed release APK:',output)
    print('Permanent signer SHA-256:',cert.fingerprint(hashes.SHA256()).hex(':').upper())

if __name__=='__main__':
    if len(sys.argv)!=5:raise SystemExit(__doc__)
    sign(*(Path(x) for x in sys.argv[1:]))
