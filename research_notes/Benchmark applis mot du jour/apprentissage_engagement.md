# Learning science and engagement mechanics for word-of-the-day / vocabulary apps (research as of Nov 2026)

Scope: what helps people retain rare words and keep using an app like Definiday (1-3 rare French words a day on the lock screen or in a notification, an evening 4-choice quiz on that day's words only, a cat mascot with unlockables, no review of past words for 9 months), and which mechanics backfire.
Evidence labels used below: **[peer-reviewed]**, **[company data]** (first-party but not peer reviewed), **[industry/opinion]** (vendor, blog or survey of stated intent), **[anecdotal]**.

## 1. Retention science: which mechanisms make rare words stick (spacing, retrieval, generation, incidental exposure, morphology, mnemonics)

### Takeaway
The two strongest, most replicated levers are **spaced re-encounters** and **retrieval practice (testing) at a delay**. Seeing a word once, plus one test a few hours later, is weak on both counts. Passive repeated exposure does help, but only modestly and only over many encounters (estimates run from about 6 to more than 20). Generation and morphology add moderate gains. The current Definiday loop (see once, test once the same evening, then nothing for 9 months) is likely to give good same-day scores and poor long-term retention.

### Cited Findings
**Spacing**
- [peer-reviewed] Cepeda, Vul, Rohrer, Wixted & Pashler (2008), *Psychological Science* 19(11), with more than 1,350 participants: people learned facts, had one review after a gap of up to 3.5 months, and took a final test up to 1 year later. Retention first rises and then slowly falls as the gap grows. The optimal gap gets longer as the test delay gets longer, but it shrinks as a proportion of the delay: about 20-40% of a 1-week delay, but only about 5-10% of a 1-year delay. — [PubMed](https://pubmed.ncbi.nlm.nih.gov/19076480/); [full text PDF](https://www.evullab.org/pdf/CepedaVulRohrerWixtedPashler-PS-2008.pdf)
- [industry/opinion, university press summary] According to York University's coverage of the study, a gap that is too short hurt retention more than one that is too long. — [York University YFile](https://yfile.news.yorku.ca/?p=9454)
- [industry docs] Anki manual: "repeating a card several times in one day does not significantly contribute to long-term memory", so it recommends keeping same-day learning steps to a minimum. — [Anki manual, Deck options](https://docs.ankiweb.net/deck-options.html)

**Retrieval practice / testing effect**
- [peer-reviewed] Roediger & Karpicke (2006), *Psychological Science* 17(3): after 5 minutes, repeated studying beat repeated testing. After 1 week the result reversed: the testing group recalled about 61% versus about 40% for the group that reread 4 times, and the study-3-times + test-once group recalled about 56% (5-minute figures about 83% vs 71%; percentages come from secondary summaries, so check the PDF before quoting them exactly). — [paper PDF](https://psychology.ecu.edu/wp-content/pv-uploads/sites/216/2019/03/Roediger-Karpicke-2006.pdf); [APS Observer summary](https://www.psychologicalscience.org/observer/test-enhanced-learning)
- [peer-reviewed] Rowland (2014) meta-analysis, *Psychological Bulletin*: testing beats restudy overall (reported g of about 0.50 over 159 effect sizes, per a secondary summary), and "initial recall tests yield larger testing benefits than recognition tests", which supports effortful processing as the mechanism. — [PubMed abstract](https://pubmed.ncbi.nlm.nih.gov/25150680/); [PDF (EPFL mirror)](https://courseware.epfl.ch/assets/courseware/v1/fdde2f0aa590bf3b1324077a6bf1540c/asset-v1%3AEPFL%2BDEMO%2B2020%2Btype%40asset%2Bblock/Rowland2014-meta-analysis.pdf)

**Generation effect**
- [peer-reviewed] Bertsch, Pesta, Wiscott & McDaniel (2007) meta-analysis, *Memory & Cognition* 35(2): 445 effect sizes from 86 studies; producing an item yourself beats reading it, d ≈ 0.40, with large variation depending on moderators. — [PubMed](https://pubmed.ncbi.nlm.nih.gov/17645161/); [PDF](https://mcdaniel97.github.io/Publications/Bertsch%20et%20al.%202007.pdf)

**Incidental / passive exposure (relevant to lock-screen display)**
- [peer-reviewed] Uchihara, Webb & Yanagisawa (2019) meta-analysis, *Language Learning* 69(3): 45 effect sizes from 26 studies (N = 1,918). The correlation between how often a word is encountered and how well it is incidentally learned is medium, r = .34. Spacing and the range of encounter counts are among the moderators. — [abstract (zendy)](https://zendy.io/title/10.1111/lang.12343); [Scinapse](https://scinapse.io/papers/2920898950)
- [peer-reviewed, via a later paper] Earlier estimates of how many exposures it takes to learn a word incidentally: 6 (Rott 1999), 8 (Horst, Cobb & Meara 1998), 10 (Webb 2007), more than 20 (Waring & Takaki 2003). Reviews note there is "no real consensus". — [Uchihara et al. 2022, UCL Discovery PDF](https://discovery.ucl.ac.uk/id/eprint/10154988/1/Uchihara%20et%20al.%20%282022%29%20Language%20Learning.pdf)
- [peer-reviewed] Webb, Uchihara & Yanagisawa (2023) meta-analysis of incidental learning: on average only 9-18% of target words were learned on immediate posttests. — [todoele bibliography entry](https://todoele.net/bibliografia/how-effective-second-language-incidental-vocabulary-learning-meta-analysis)
- [peer-reviewed] Webb, Yanagisawa & Uchihara (2020), *Modern Language Journal* 104(4), a meta-analysis of intentional activities (flashcards, word lists, writing, fill-in-the-blanks; 100 effect sizes from 22 studies): learning from word-focused tasks "was far from guaranteed", and the direction of learning (L2→meaning vs meaning→L2) moderates outcomes. — [x-mol abstract](https://www.x-mol.com/paper/1341861690617110528)
- [peer-reviewed, small sample] Cai et al. (CHI 2015), "Wait-Learning" (WaitChatter): vocabulary and micro-quizzes shown during the short waits in instant messaging. Two-week field study, n = 20; users were most receptive right after sending a message; about 57 words learned per person (≈4/day). No long-term retention data found. — [CHI 2015 PDF](https://sls.csail.mit.edu/publications/2015/Cai_CHI-2015.pdf); [MIT News](https://news.mit.edu/2015/learn-language-while-you-text-0514)
- [industry/opinion] Lock-screen vocabulary apps (LearnLock, Vocab Vault Pro, Memorio, WordBit) claim retention benefits from "repeated exposure" without citing studies. Some pair the display with spaced repetition or quizzes, on the reasoning that "active recall complements passive exposure". — [Vocab Vault Pro listing](https://apps.apple.com/us/app/id6759111391); [Widget Vocabulary listing](https://mwm.ai/apps/widget-vocabulary-learn-words/6775767149)

**Morphology / word parts**
- [peer-reviewed] Goodwin & Ahn meta-analyses of morphological instruction, English, school-age children: vocabulary effect d ≈ 0.34-0.40. — [Goodwin & Ahn 2013 (Taylor & Francis)](https://informahealthcare.com/doi/full/10.1080/10888438.2012.689791); [typeset summary](https://typeset.io/pdf/a-meta-analysis-of-morphological-interventions-effects-on-v0qrdin8yg.pdf)
- [peer-reviewed, mixed] Some studies find no direct effect of morphological knowledge on vocabulary size, only an indirect effect through morphological awareness. — [Frontiers in Psychology 2023](https://www.frontiersin.org/journals/psychology/articles/10.3389/fpsyg.2023.1207854/pdf)

### Inferences
- Showing the word on the lock screen and then quizzing it the same evening amounts to "massed study + one recognition test at a very short delay". Cepeda's ridgeline suggests that if a word should still be known months later, at least one re-encounter should come days to weeks after first exposure, not hours. A 9-month gap is far beyond the optimum, and a same-day quiz is far below it.
- Lock-screen display is a reasonable *exposure* channel, but it is closer to "restudy" than to "retrieval". It probably shows its value mainly when combined with later retrieval (a quiz on older words), not on its own.
- A low-cost generation boost: before revealing the definition, ask "guess the meaning" or "which root?". Etymology and morphology content (Latin/Greek roots, which are common in rare French words) is a plausible encoding aid, but the strong evidence comes from children learning L1 English. For adult French native speakers learning rare French words, it is an extrapolation.
- Rare words are low-frequency by definition, so users will almost never meet them in real life. The app must therefore supply the repetitions itself.

### Gaps
- No study found that specifically tests passive lock-screen or notification exposure to word+definition pairs with delayed retention tests. The Dingler et al. "opportune moments" work did not appear in searches.
- Keyword mnemonics and images (dual coding), audio pronunciation, and context sentences vs isolated definitions were not covered by reliable sources in this session. Well-known findings exist (e.g. the keyword method, the involvement load hypothesis of Laufer & Hulstijn) but were not verified here and should not be cited without checking. A meta-analysis on the involvement load hypothesis exists ([sciencegate](https://next.sciencegate.app/document/10.1111/lang.12444)), but its results were not retrieved.
- Nearly all vocabulary-learning research covers L2 learners. Evidence on L1 adults learning rare words of their own language (Definiday's exact case) is thin.

## 2. Quiz formats: recognition vs recall, multiple choice, cloze, "use the word"

### Takeaway
Recall formats (type or produce the word) produce more durable learning than recognition, but multiple choice can work well **if the wrong options are plausible and competitive**. Definiday's format (definition shown → pick one of 4 words) is a reasonable base. Its strength depends on how good the distractors are, and it could be complemented by a harder format for words seen before.

### Cited Findings
- [peer-reviewed] Rowland (2014): initial recall tests yield larger benefits than recognition tests. — [PubMed](https://pubmed.ncbi.nlm.nih.gov/25150680/)
- [industry/opinion summarizing Rowland] Final-test subset figures reported by a secondary source: cued recall g = 0.70, free recall g = 0.79, recognition g = 0.32. Not verified in the original paper. — [Mindomax](https://www.mindomax.com/the-testing-effect)
- [peer-reviewed] Little, Bjork, Bjork & Angello (2012) and Little & Bjork (2014/2015): multiple-choice tests with **competitive** distractors improve retention of the tested item *and* of related information tied to the wrong options. The mechanism is that learners retrieve why each distractor is wrong. Cued recall did not produce the related-information benefit. Writing good competitive distractors is hard. — [Little & Bjork 2014 PDF (UCLA Bjork Lab)](https://bjorklab.psych.ucla.edu/wp-content/uploads/sites/13/2017/01/LittleBjorkMC2014.pdf); [Springer CR:PI 2016](https://link.springer.com/article/10.1186/s41235-016-0003-x); [retrievalpractice.org](https://www.retrievalpractice.org/strategies/2018/multiple-choice)
- [industry/opinion] Multiple-choice quizzes overstate learning because recognition is easier than recall. — [eLearning Industry](https://elearningindustry.com/recognition-is-not-recall-why-your-multiple-choice-quizzes-overstate-learning)
- [peer-reviewed] Webb et al. (2020): the direction of the test (form→meaning vs meaning→form) moderates outcomes. — [x-mol](https://www.x-mol.com/paper/1341861690617110528)

### Inferences
- With only 1-3 words per day, at least 1-3 of the 4 options per question must come from elsewhere. Drawing those distractors from **past words the user has already seen** would turn every quiz into a hidden review of older words. It would also make the distractors competitive (same register, rare, similar morphology), which is the condition Little & Bjork identify for multiple-choice tests to aid learning.
- A 100%-correct same-evening quiz mostly measures short-term recognition. Using "perfect quiz" as an unlock trigger rewards an easy metric. Rewarding delayed correct answers (on a word seen ≥ 7 days earlier) would match the reward to real retention.
- A progression ladder supported by the literature: (a) choose the word for a definition (current), (b) choose the definition for a word, (c) cloze sentence with 4 options, (d) type the first letters / free recall for well-known words. This ladder is an inference from the recall > recognition finding; no study was found that tests this exact sequence.

### Gaps
- No direct evidence found comparing cloze sentences vs definition matching for rare L1 words.
- No evidence retrieved on "use the word in a sentence" prompts (production tasks) in apps. Writing tasks appear in Webb et al. 2020, but per-activity effect sizes were not retrieved.

## 3. Is reviewing only the same day's words enough? How Anki, Duolingo and others schedule reviews

### Takeaway
No. The research consensus and every major spaced-repetition system bring items back at expanding intervals of days to months, tuned to a target recall probability (about 90% by default in Anki/FSRS). A 9-month no-repeat rule with no intermediate review goes against everything found here. Duolingo moved from a Leitner system to a learned model (half-life regression) and reports better predictions and higher engagement.

### Cited Findings
- [industry docs] Anki with FSRS: the default desired retention is 90%, which "offers a good balance of retention and workload". Workload rises exponentially with retention: "above 90% the workload increases very quickly, and above 97% the workload can be overwhelming." Learning steps should be under a day, and FSRS can drive scheduling. — [Anki manual](https://docs.ankiweb.net/deck-options.html)
- [industry/open benchmark] The open-spaced-repetition benchmark (about 20k Anki collections, about 729M reviews, same-day reviews excluded) compares FSRS with SM-2 and others. The SM-2 tested there is the original algorithm with extra formulas added to produce probabilities, not Anki's variant. Secondary claims that FSRS beats SM-2 for about 97-99% of users were not verified on the benchmark page itself. — [Expertium benchmark page](https://expertium.github.io/Benchmark.html); [Anki FAQ on algorithms](https://docs.ankiweb.net/faqs/what-spaced-repetition-algorithm.md)
- [peer-reviewed + company data] Settles & Meeder (ACL 2016), "A Trainable Spaced Repetition Model for Language Learning": half-life regression (HLR) estimates each word's memory half-life. It cut prediction error by 45%+ versus baselines, and its error was roughly half that of Leitner, the system Duolingo's first version used. The abstract reportedly states HLR raised daily student engagement by 12% in an operational study (quoted on a mailing list, not verified in the PDF). The code and 13M learning traces are public. — [ACL Anthology PDF](https://preview.aclanthology.org/fix_video/P16-1174.pdf); [Duolingo blog](https://blog.duolingo.com/how-we-learn-how-you-learn); [GitHub](https://www.github.com/duolingo/halflife-regression); [mailing-list quote](https://www.mail-archive.com/mnemosyne-proj-users@googlegroups.com/msg09645.html)
- [peer-reviewed] Cepeda et al. (2008): for retention at 1 year, the optimal single review gap is about 5-10% of the retention interval, i.e. roughly 3-5 weeks. — [PubMed](https://pubmed.ncbi.nlm.nih.gov/19076480/)
- [industry] A competitor-type app (Vocabulex) exposes desired retention and maximum interval settings for adaptive spaced repetition. — [App Store](https://apps.apple.com/app/id6757659130)
- [industry] Vocabulary – Learn words daily (Monkey Taps): word-of-the-day app for rare English words, more than 10,000 words, about 4.8★ from roughly 190k+ ratings, minimal "scroll to learn" interface. Searches did not confirm how or whether it schedules review of past words. — [App Store](https://apps.apple.com/app/id1084540807); [appwisp](https://appwisp.com/app/ios/com.MonkeyTaps.Vocabulary)

### Inferences
- A minimal fix that keeps "no account, offline, simple": keep the 9-month rule for *new-word presentation*, but add a light review layer, for example 1 review question per evening quiz on a word seen about 2 days, 1 week, 1 month and 3 months earlier (a fixed Leitner-like ladder). The full FSRS library is unnecessary at Definiday's scale of 1-3 words/day. A fixed expanding ladder captures most of the spacing benefit; FSRS can come later if users answer many reviews. This is an inference: no study compares a fixed ladder with FSRS for so few items.
- The past-word distractors idea from section 2 provides "free" reviews with no extra screens.
- A favourites/history archive is the natural home for past words, and it lets users choose words to review on their own (user-chosen review lists are standard in Anki and Quizlet).

### Gaps
- Review scheduling in WordUp, Memrise, Quizlet and Monkey Taps' Vocabulary was not confirmed from primary sources this session. Do not assert specifics.
- No public data on the review intervals or retention outcomes of word-of-the-day apps specifically.

## 4. Engagement: streaks, mascots/collections, notifications, goals, progress, lists, social, calm vs gamified, churn

### Takeaway
Streaks work. Duolingo's data are strong, and the bar to keep a streak alive should be **low** (one action). Broken streaks reduce later engagement unless the user can repair them, and a single missed day does not hurt habit formation. Notifications: few, relevant, varied, and timed to natural moments; stated-intent surveys show users disabling notifications or leaving apps at 2-10 pushes/week. Evidence on companion pets/mascots (Finch) is mostly anecdotal but consistent: self-set goals plus emotional attachment, with guilt as the failure mode.

### Cited Findings
**Streaks**
- [company data] Duolingo: learners who reach a 7-day streak are 2.4× more likely to come back the next day. Separating the streak from the daily goal, so that one lesson extends the streak, raised Day-14 retention by +3.3% and DAU by +1% (relative). Learners on a 7+ day streak rose by more than 40%. One year later, just over half of daily learners had a 7+ day streak (up from about a third). Trade-off: fewer learners met their daily goal. Learners on the "intense" goal were least likely to have a streak. — [Duolingo blog, "Improving the streak"](https://blog.duolingo.com/improving-the-streak)
- [company data, podcast] Duolingo's retention PM discusses streak freezes, the change to a one-lesson streak rule, and "1 → 2-day streak is a huge jump in retention". Exact freeze numbers were not retrieved. — [Lenny's Newsletter](https://lennysnewsletter.com/p/behind-the-product-duolingo-streaks)
- [peer-reviewed] Silverman & Barasch (2023), *Journal of Consumer Research* 49(6), 7 studies: intact streaks shown in a log increase later engagement compared with broken streaks shown in the log. The effect depends on *how the log represents* behavior, not on the behavior itself. It is amplified when users blame themselves for the break and attenuated when the streak can be "repaired". Author advice: don't focus messaging on broken streaks; offer alternate ways to keep them going. — [UDel repository](https://udspace.udel.edu/items/42ce576b-8e1f-429a-8541-e29e48dbcfbb); [CU Boulder summary](https://www.colorado.edu/business/faculty-research/2023/04/19/or-track-how-broken-streaks-affect-consumer-decisions); [UDaily](https://udel.edu/udaily/2024/march/power-of-streaks-motivation-jackie-silverman)
- [peer-reviewed] Lally et al. (2010), *European Journal of Social Psychology*: 96 participants, 82 completed; median/average ≈ 66 days to reach peak automaticity (range 18-254). A single missed day had little long-term impact, but repeated misses reduced the peak. — [BPS Research Digest](https://bps.org.uk/research-digest/how-form-habit); [spring.org.uk summary](https://www.spring.org.uk/2023/01/form-a-habit.php)
- [industry/opinion] Streak anxiety and burnout are widely discussed. The figures circulating (e.g. "CHI 2020 study: 63% higher quit rate after one missed day") could not be traced to a primary source and should be treated as unverified. — [Yu-kai Chou](https://yukaichou.com/gamification-analysis/streak-design-gamification-motivation-burnout/); [habitdoom blog](https://habitdoom.com/blog/streak-anxiety-habit-trackers)

**Notifications**
- [peer-reviewed + company data] Yancey & Settles (KDD 2020), "A Sleeping, Recovering Bandit Algorithm for Optimizing Recurring Notifications" (Duolingo): new notification templates boost short-term response, and the effect fades with repetition (a "recovering arm" effect), so message variety matters. The reported gains are about +0.5% total DAU and +2% new-user retention (from a garbled listing; verify in the PDF). A dataset of 200M practice-reminder notifications was released. — [paper PDF](https://research.duolingo.com/papers/yancey.kdd20.pdf); [SOTAVerified listing](https://sotaverified.org/papers/a-sleeping-recovering-bandit-algorithm-for)
- [peer-reviewed, small n] WaitChatter: users were most receptive to micro-learning right after finishing an action (sending a message). — [CHI 2015 PDF](https://sls.csail.mit.edu/publications/2015/Cai_CHI-2015.pdf)
- [industry/opinion, stated-intent surveys] Localytics 2017 (n ≈ 1,000 US): about 10% would disable pushes at 1/week; 22% would stop using an app at 2-5/week; 31% at 6-10/week. A 2014 UK DMA survey: 80% would disable or uninstall if unhappy with notification volume. Irrelevant, repetitive messages are a common uninstall reason (Braze). — [Business of Apps](https://www.businessofapps.com/marketplace/push-notifications/research/push-notifications-statistics/); [MarketingProfs](https://www.marketingprofs.com/charts/2018/33486/how-many-mobile-push-notifications-are-too-many); [Mobile Marketing Magazine](https://mobilemarketingmagazine.com/?p=90128); [Braze](https://www.braze.com/resources/articles/app-uninstalls-totally-unscientific-look-makes-customers-pull-plug.md)

**Mascots, pets, collections, goals (Finch and cosy apps)**
- [industry/opinion] Finch: users set their *own* goals and earn energy for a virtual bird, which reviewers highlight as a key design choice. — [Makeheadway review](https://makeheadway.com/blog/finch-app-review/); [Engadget](https://www.engadget.com/apps/this-self-care-virtual-pet-is-helping-me-get-my-act-together-160027169.html)
- [anecdotal] App review analysis links continued use to emotional attachment to the bird (correlational, from reviews). — [Appbot](https://appbot.co/blog/finch-app-reviews-emotional-attachment-user-retention-product-loyalty/)
- [anecdotal] Failure modes: guilt when a lapsed user received "your pet wants to share something" notifications ([IU blog](https://geneticslab.medicine.iu.edu/blogs/md-student-news/wellness-corner-a-digital-pet-is-your-self-care-best-friend)); "gamification fatigue", where pet care feels like another chore ([Neurospicy](https://neurospicynomnoms.beehiiv.com/p/digital-pet-bestie-for-self-care-610618d407f7afef)); paywalled care items make free users feel they give "partial care" ([Pratt IxD critique](https://ixd.prattsi.org/2025/09/design-critique-finch-ios-app-2/)).
- [industry/opinion] Duolingo, trading user experience against goals: separating the streak from the daily goal meant fewer goals met but more retention, i.e. consistency matters more than volume early on. — [Duolingo blog](https://blog.duolingo.com/improving-the-streak)

### Inferences
- Definiday's cat unlocks tied to *quiz counts* and *streaks* match the mechanics with the best evidence (low-bar daily action, collection). To avoid the Silverman & Barasch penalty, show streaks so a break doesn't read as failure. Options: a weekly view ("5/7 days this week") instead of a fragile counter, or a free streak repair or freeze earned by playing. Never make the cat look sad or "lose" items after a missed day.
- Because Lally found one miss harmless, a "grace day" is scientifically justified, not just a nicety.
- Notifications: the lock-screen word is already a daily touchpoint. Adding at most one evening quiz reminder, with varied wording (the recovering-arm effect) and an easy off switch, keeps volume well below the 2-5/week range where surveys show attrition.
- Progress visualisation should measure *retention* (e.g. "words still remembered after 1 month") rather than raw quiz counts, to avoid the gamification misuse in section 5.
- Favourites, history and sharing a word (no account needed, via the Android share sheet) are cheap. They give users ownership (Finch's self-set goals logic) and provide material for review.

### Gaps
- No primary data found on churn causes specific to vocabulary or word-of-the-day apps. App-review mining (Play Store reviews of Monkey Taps' Vocabulary, WordBit, etc.) would be needed.
- No controlled evidence found on "calm/slow" apps versus gamified ones for long-term retention or learning.
- Duolingo streak-freeze effect sizes and social/leaderboard effects were not retrieved from primary sources.
- Evidence on the best notification *time of day* for vocabulary is limited to WaitChatter's "after a natural action" finding and Duolingo's bandit work on templates. Nothing was found specifically on evening quiz timing.

## 5. Ethical / dark-pattern concerns (guilt notifications, mocking mascots) and how cosy apps avoid them

### Takeaway
Peer-reviewed HCI work now explicitly flags Duolingo-style mechanics (guilt-laden notifications, emotionally charged mascot visuals, "gamified obligation" at exit, fixation on streaks/XP) as deceptive or harmful patterns, especially for children. The safer design pattern is: user-set goals, forgiving streaks, no punishment for absence, and rewards tied to learning rather than to time on the app.

### Cited Findings
- [peer-reviewed] Almeida Mogavi et al. (2022), "When Gamification Spoils Your Learning": a qualitative study of 9 years of Duolingo forum data plus 15 interviews. "Gamification misuse" (users focus on game elements instead of learning) is driven by competitiveness, overindulgence in playfulness and herding. Leaderboards, badges and points are implicated. Harms include wasted time, worse learning, well-being and ethics problems. — [arXiv](https://arxiv.org/abs/2203.16175)
- [peer-reviewed] Castro & Valença (IHC 2025), "Teaching or Manipulating? On the Adoption of Bright and Deceptive Patterns by Duolingo": finds some ethical (bright) patterns, such as clear interfaces, but also manipulative ones, notably excessive notifications and emotionally charged visuals, assessed against children's rights. — [SBC OpenLib](https://sol.sbc.org.br/index.php/ihc/article/view/37677); [deceptive.design summary](https://deceptive.design/articles/teaching-or-manipulating-on-the-adoption-of-bright-and-deceptive-patterns-by-duolingo)
- [peer-reviewed/advocacy summary] Kuo, Zhao & Scott, "The Evil Bird and the Right to Disconnect": interviews with parent-child pairs describe "gamified obligation", i.e. guilt or disappointment on trying to leave the app, as an exit dark pattern that raises the cost of quitting. — [deceptive.design](https://deceptive.design/articles/the-evil-bird-and-the-right-to-disconnect-children-s-vulnerability-to-exit-dark-patterns-in-gamified-educational-apps)
- [industry/opinion] "Duolingo's Owl Scares Me: An Introduction to Trauma-centered Design" (Socitm/Silktide webinar): high-pressure notifications as a stress source. — [Socitm](https://socitm.net/resource-hub/webinars/duolingos-owl-scares-me-an-introduction-to-trauma-centered-design/)
- [peer-reviewed] Silverman & Barasch (2023): blaming the user for a break amplifies disengagement, and repair options attenuate it. Guilt framing is therefore both ethically doubtful *and* counter-productive. — [CU Boulder summary](https://www.colorado.edu/business/faculty-research/2023/04/19/or-track-how-broken-streaks-affect-consumer-decisions)
- [anecdotal] Finch lapsed-user guilt from pet notifications ([IU blog](https://geneticslab.medicine.iu.edu/blogs/md-student-news/wellness-corner-a-digital-pet-is-your-self-care-best-friend)). An unsourced claim says Finch's aesthetic deliberately reduces "guilt-debt" ([appireview](https://appireview.pages.dev/com.finch.finch), unverified).

### Inferences
- Rules that follow for Definiday's cat: the cat never sulks, mocks or "suffers" from absence; unlocks are never removed; no "your cat misses you" pushes after inactivity (or at most one gentle message, then silence); a return after absence is greeted positively (a "welcome back" pose), which fits the repair finding.
- Definiday's existing choices (no ads, no account, offline, no leaderboards) already avoid the main mechanics implicated in Mogavi et al. (competition, herding) and in ad-driven pressure. Adding social leaderboards would bring back the documented misuse risk. Simple sharing of a word is lower risk.
- Since rewards that track learning reduce "playing the game instead of learning", rewards could favour delayed-recall successes over raw quiz counts or same-day perfect scores (see section 2).

### Gaps
- No quantitative study found measuring how much guilt-style notifications raise or lower long-term retention compared with neutral ones. The evidence is qualitative or indirect (Silverman & Barasch).
- Duolingo's own published reasoning or data on the "passive-aggressive owl" persona was not retrieved.
- No formal case study of Finch's retention metrics was found; all Finch claims are review-based or anecdotal.
