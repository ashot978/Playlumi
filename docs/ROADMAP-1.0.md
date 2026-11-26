# Playlumi — 0.99 release candidate and roadmap to 1.0

Status: 0.99.0 / build 600990. Test build, not a production launch.

## 0.99: release candidate
- [x] Review age-specific catalogue and check existing quiz generators for obvious copy-answer or ambiguous prompts.
- [x] Analog clocks: whole hours, quarter hours and five-minute steps by level; no 24-hour ambiguity without day part. Hour shifts retain minutes.
- [x] Weather scenarios with a specific question and one intended response. Safer crossing and found-medicine examples.
- [x] Star Catch awards normal progress once per completed round. Interactive correct steps count as a play day.
- [x] Parent information about local storage, offline voice and deleting all app data.
- [x] Mandatory dependency-free seeded Java regression gate for all catalogue routes, adaptive levels and legacy quiz configurations. Full Android unit tests and lint after APK packaging.
- [ ] Confirm gate, APK package and all existing unit tests/lint on CI; document any failures.
- [ ] Sign with original release key and verify SHA-256 certificate and APK integrity.
- [ ] Physical-device tests on compact phone, opened Fold, tablet (portrait and landscape) and Android 15+ insets.
- [ ] Run every exposed title at each supported age; confirm educational content and uniquely defensible answers. Generated tests are not a substitute for manual content acceptance.

## 0.99.x: fix only issues found during release candidate testing
- [ ] Test update from 0.98.1 without losing stars, achievements, favourites or unfinished rounds.
- [ ] Verify navigation, fold/unfold, rotation, back, multi-profile isolation and parent gate.
- [ ] Verify screen reader, text sizing, offline Dutch voice present/absent, muted audio and reduced motion.
- [ ] Test long sessions and app background/foreground, no repeated awards.
- [ ] Review all wording and artwork licensing; final privacy notice, store Data Safety and target audience declarations must match the actual app and distribution.

## 1.0: only after approval
- [ ] No known release-blocking crashes, content errors, lost progress or signing incompatibility.
- [ ] Complete remaining real-device and store-policy checks; confirm closed/internal tester acceptance.
- [ ] Prepare approved listing, screenshots, privacy URL, release notes and signed production AAB.
- [ ] Owner expressly approves submission. No automatic Play Store publication.

## After 1.0
Consider new games, more original illustration/audio and expanded parent reports only after release stability. No ads, paid streak recovery, notifications pressuring daily play, or loss of earned badges.
