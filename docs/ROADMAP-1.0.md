# Playlumi — 0.99 release candidate and roadmap to 1.0

Status: 0.99.2 / build 600992. Test build, not a production launch.

## 0.99: release candidate
- [x] Review age-specific catalogue and check existing quiz generators for obvious copy-answer or ambiguous prompts.
- [x] Analog clocks: whole hours, quarter hours and five-minute steps by level; no 24-hour ambiguity without day part. Hour shifts retain minutes.
- [x] Weather scenarios with a specific question and one intended response. Safer crossing and found-medicine examples.
- [x] Star Catch awards normal progress once per completed round. Interactive correct steps count as a play day.
- [x] Parent information about local storage, offline voice and deleting all app data.
- [x] Mandatory dependency-free seeded Java regression gate for all catalogue routes, adaptive levels and legacy quiz configurations. Full Android unit tests and lint after APK packaging.
- [x] 0.99.0 gate, APK package and full unit tests/lint passed on Actions run 45894018546.
- [x] 0.99.1 gate, APK package and full unit tests/lint passed on Actions run 45897062350.
- [ ] Repeat all checks for 0.99.2.
- [x] 0.99.0 signed and verified with existing release certificate.
- [ ] Verify signed 0.99.1 APK.
- [ ] Physical-device tests on compact phone, opened Fold, tablet (portrait and landscape) and Android 15+ insets.
- [ ] Run every exposed title at each supported age; confirm educational content and uniquely defensible answers. Generated tests are not a substitute for manual content acceptance.

## 0.99.1: resume and lifecycle fixes
- [x] Save and resume minigames, memory matches, melodies, bubbles and stars independently per age/profile/game. No repeat credit for completed rounds.
- [x] Keep exact minigame state through configuration recreation and pause reminder; commit navigation before visual transitions.
- [x] Stop delayed music preview on pause or navigation; relock parent access on leaving the parent area.
- [x] Expand rhyme choices and prevent advanced letter/rhyme quizzes from silently dropping to the middle age tier.
- [x] Add 1,620 deterministic mini state restoration checks to the release gate.
- [ ] Confirm end-to-end restoration and reward counts on actual phones and Fold.

## 0.99.2: improved everyday play
- [x] Search by title, description and relevant keywords, within the chosen age group.
- [x] Dedicated complete list of unfinished games ordered by recent use.
- [x] Optional strategy hints, previously attempted answer state and explanations after solving; automatic next question remains.
- [x] More compact short-answer layouts and filled numeric sequences after solving.
- [ ] Full release gate, APK signature, unit tests and lint.
- [ ] Manual acceptance of search, answer selection and Fold layout.

## 0.99.x: remaining acceptance checks
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
