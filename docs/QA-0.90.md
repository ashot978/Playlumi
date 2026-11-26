# Playlumi 0.90 - evaluation checklist

This is a development APK, not a Play Store release. Version 0.90.0 / 600900.

## Automated fast path
- Compile production APK; verify package, version, approved illustration hashes.
- Verify release signature against the existing release certificate (automatic with configured secrets; otherwise private offline signing).
- Full unit tests and Android Lint are optional via workflow_dispatch with full_qa.

## Physical device acceptance (not yet performed)
1. Install over 0.89 without clearing data. Compare stars, outfits, favorites and existing treasure progress.
2. Start two quizzes and two interactive activities; solve at least one step in each, return to library, close/reopen app. Resume each independently; check solved questions never award points twice.
3. Upgrade with a 0.89 saved session; verify migration, then complete it and ensure the old record does not reappear.
4. Check cover and unfolded Fold, tablet landscape, system back and gesture/three-button navigation. Confirm library tabs, long titles, answer tiles, and returning to the prior scroll position.
5. Navigate Rewards > Closet > Back, Companion > Character > Back, and both entry points to Treasures. Check real accessory preview, purchases with earned stars, and no repeated charges.
6. Verify sound disabled and reduced motion enabled. Test all age groups and ensure profile-specific saves stay separate.
7. Complete a quiz, replay and test the four interactive games. Confirm scores and rewards. Review music, mini-games, drawing, garden and existing adventures manually.
8. Review privacy/offline behavior, child accessibility and parental controls before considering 1.0.

Known limits: independent durable resumes cover catalog quizzes and four interactive activities. Other activity-specific resume behavior is unchanged. Full visual and device acceptance remain pending. No production store deployment is authorized.
