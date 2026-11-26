# Playlumi — 0.99 release candidate and roadmap to 1.0

Status: 0.103 / build 601004. Test build, not a production launch. Future builds use 0.104, 0.105, etc., and strictly increasing Android version codes.

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
- [x] 0.99.2 release gate, APK signature, full unit tests and lint passed on Actions run 45897467652.
- [ ] Manual acceptance of search, answer selection and Fold layout.

## 0.99.3: learning depth and pace
- [x] 28 curated Dutch spelling contexts for the oldest age tier, with specific hints and explanations.
- [x] Letter matching draws from 10, 18 or 26 letters according to level; still only 3, 4 or 6 choices.
- [x] More missing-letter positions and complete visual patterns after solving.
- [x] Automatic, calm and manual quiz progression per profile; existing automatic mode remains default.
- [x] Additional 2,500 spelling and 3,600 letter regression samples and pacing checks.
- [x] 0.99.3 build, signature, unit tests and lint verified, Actions run 45899377905.
- [ ] Real-device interaction.
 
## 0.99.4: interactive learning depth
- [x] Number race uses distractors for middle and older children, skip counting and descending sequences for older children.
- [x] Letter train uses picture and word clues, incomplete examples and optional reveal instead of mandatory copying at older ages.
- [x] Version interactive seeds so legacy unfinished rounds retain their original rules and tiles.
- [x] Preserve final manual-pace quiz feedback across activity recreation.
- [x] Additional 1,440 versioned interactive sessions tested by the release gate.
- [x] 0.99.4 build, signing, Android unit tests and lint verified, Actions run 45900454569.
- [ ] Real-device interaction.

## 0.99.5: advanced arithmetic
- [x] Three additional 7+ routes: basic multiplication tables, exact division, and mixed addition/subtraction to 100.
- [x] Separate route IDs retain existing arithmetic, favorites, and save slots.
- [x] Correct quiz hints, explanations, speech operators and restored variant selection.
- [x] Additional 3,600 advanced arithmetic regression samples.
- [x] 0.99.5 full build, signature, Android unit tests and lint verified on Actions run 45901750270.
- [ ] Real-device acceptance.

## 0.99.6: completion and celebration
- [x] Reusable responsive completion panel for quizzes, story missions, minigames, music, stars and interactive boards.
- [x] Display base round reward and actual saved wallet balance separately; rendering cannot grant currency.
- [x] New persistent per-profile and age-specific completion marks, dedicated library filter, and count in achievements.
- [x] Mark only completed catalog routes after the existing reward event; no guessed backfill for previous versions.
- [x] Reuse scalable medal artwork and adapt medal size to screen height.
- [x] Additional catalog completion and replay checks.
- [ ] Verify full CI, signing and physical layout, including long labels and Fold.

## 0.100: simplified quiz interface and versioning
- [x] Remove the quiz hint row, including hints after incorrect answers.
- [x] Remove spoken instructions from game screens and parent settings; keep music, feedback sounds and Android accessibility.
- [x] Hide the quiz continuation row until a correct answer; keep manual and automatic continuation and existing progress.
- [x] Use the actual APK version in settings and remove obsolete voice information from the privacy dialog.
- [x] 0.99.7 release compilation, artwork verification, tests and lint passed on Actions run 45902862452.
- [x] Interim 1.0 (build 601000) compiled and passed unit tests/lint; superseded by the requested 0.100 numbering.
- [x] 0.100 production APK, tests and lint passed on Actions run 45907665732; signed offline with the existing release certificate and verified ZIP alignment and payload integrity.
- [ ] 0.100 emulator checks still pending; prior emulator test stopped at home navigation before reaching the quiz.
- [ ] Physical-device acceptance still required. Version 0.100 is not authorization to publish.

## 0.101: resume at the chosen quiz pace
- [x] Keep the solved question and manual Next action when reopening a saved unfinished quiz in manual mode; automatic and calm modes continue to the next question.
- [x] Restore rejected choices without showing old hints; preserve whether a wrong answer was attempted.
- [x] Reset stale final-feedback state for new and resumed sessions.
- [x] Regression test for restored solved questions, disabled rejected choices, and prevention of repeat answer acceptance.
- [ ] Run full CI, verify signing and emulator acceptance.

## 0.102: save a quiz from its first question
- [x] Persist an opened catalog quiz at 0/8 even before the first answer, including the exact question and answer order.
- [x] Validate saved quiz counts and solved flags; exclude completed and inconsistent sessions.
- [x] Restrict quiz snapshot writes to catalog play; retain independent age/profile/route keys.
- [x] Restore the last played game and clear outdated feedback before saving the next question.
- [x] Add unit tests for zero-attempt, wrong-answer, solved, completed and inconsistent session states.
- [ ] Verify full CI, signing and device acceptance.

## 0.103: robust quiz restoration
- [x] Restore rejected answers by their original index mapping when legacy spatial options are reordered.
- [x] Validate every saved answer and color; invalid question data never replaces a valid generated question.
- [x] Preserve valid session counters and regenerate only a damaged saved question without awarding points.
- [x] Add regression tests for legacy reordering and invalid choices.
- [x] 0.102 production APK, unit tests, lint and offline signing verified (Actions run 45908000090).
- [ ] Verify 0.103 full CI, signing and device acceptance.

## Remaining acceptance checks
- [ ] Test update from 0.98.1 without losing stars, achievements, favourites or unfinished rounds.
- [ ] Verify navigation, fold/unfold, rotation, back, multi-profile isolation and parent gate.
- [ ] Verify screen reader, text sizing, muted audio, reduced motion, and absence of app-generated speech.
- [ ] Test long sessions and app background/foreground, no repeated awards.
- [ ] Review all wording and artwork licensing; final privacy notice, store Data Safety and target audience declarations must match the actual app and distribution.

## 1.0: only after approval
- [ ] No known release-blocking crashes, content errors, lost progress or signing incompatibility.
- [ ] Complete remaining real-device and store-policy checks; confirm closed/internal tester acceptance.
- [ ] Prepare approved listing, screenshots, privacy URL, release notes and signed production AAB.
- [ ] Owner expressly approves submission. No automatic Play Store publication.

## After 1.0
Consider new games, more original illustration/audio and expanded parent reports only after release stability. No ads, paid streak recovery, notifications pressuring daily play, or loss of earned badges.
