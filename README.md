# Now Brief — Live Capsule (Android 16 / API 36)

تطبيق أندرويد محلي بالكامل (Kotlin) يعرض كبسولة "Now Brief" في شريط الإشعارات
الحي (Live Update) وكويدجت على الشاشة الرئيسية، بدون أي استهلاك خلفي للبطارية.

## بنية المشروع
```
NowBriefCapsule/
├── build.gradle.kts, settings.gradle.kts, gradle.properties
├── gradle/wrapper/gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts              # compileSdk = targetSdk = 36
│   ├── src/main/AndroidManifest.xml  # صلاحيات Live Update + Receivers
│   └── src/main/java/com/nowbrief/livecapsule/
│       ├── TimeUtils.kt              # منطق الفترات الأربع (نقي، بدون I/O)
│       ├── PeriodAlarmScheduler.kt   # ينظّم إنذار واحد فقط للحد القادم
│       ├── PeriodAlarmReceiver.kt    # يوقظ التطبيق 4 مرات/يوم كحد أقصى
│       ├── BootReceiver.kt           # يعيد ضبط الإنذار بعد إعادة الإقلاع
│       ├── NowBriefNotifier.kt       # يبني إشعار Live Update (API 36)
│       ├── MainActivity.kt
│       └── widget/NowBriefWidgetProvider.kt
└── .github/workflows/build.yml       # بناء APK تلقائياً عبر GitHub Actions
```

## قرارات هامة بخصوص الملفات المرفقة

- **colors.xml**: الملف الذي أرفقته هو تفريغ (dump) ناتج عن فك حزمة APK بأداة
  `apktool` لتطبيق سامسونج نفسه (تظهر فيه أسماء داخلية مثل `sesl_*`،
  `ai_suggestion_app_widget_*`، وعناصر `APKTOOL_DUMMY_*` التي تتركها الأداة).
  هذه موارد تصميم خاصة بسامسونج مستخرجة من برنامج مُفكك، ونسخها حرفياً داخل
  مشروع جديد يُعد استخداماً لملكية فكرية لا أملك ترخيصاً لإعادة توزيعها. لذلك
  بنيت **لوحة ألوان أصلية** (`app/src/main/res/values/colors.xml`) بروح
  تدرجات "Now Brief" (أربع تدرجات فترات + لون محايد) لكن بقيم مختلفة تماماً.
- **خط SamsungOne.ttf**: لم يكن مرفقاً فعلياً في الملفات التي رفعتها (المرفقات
  كانت `colors.xml` و3 ملفات SVG فقط)، وهو أساساً خط سامسونج المملوك حصرياً
  لها وغير متاح للتوزيع الحر. استخدمت `sans-serif-medium` كبديل نظامي مؤقت في
  `now_pill_layout.xml` و`activity_main.xml`. إن كان لديك ترخيص مشروع لاستخدام
  هذا الخط، ضع الملف في `app/src/main/res/font/samsungone.ttf` وبدّل
  `android:fontFamily="sans-serif-medium"` إلى `@font/samsungone`.
- **أيقونات SVG الثلاث**: أشكال بسيطة عامة (خطوط هندسية أساسية)، فأعدت
  ترميزها كـ VectorDrawable في `ic_now_brief.xml` (أيقونة الإشعار، لون أحادي
  أبيض كما يتطلب النظام) وفي `ic_launcher_foreground.xml` (أيقونة التطبيق).

## Live Update / Now Bar على أندرويد 16
منصة "Live Update" في أندرويد 16 (API 36) **لا تقبل RemoteViews مخصصة** —
تشترط أحد الأنماط القياسية فقط (BigTextStyle / CallStyle / ProgressStyle)
بالإضافة إلى:
- صلاحية `POST_PROMOTED_NOTIFICATIONS` في الـ Manifest (مُضافة).
- `setOngoing(true)` + طلب الترقية عبر `EXTRA_REQUEST_PROMOTED_ONGOING`
  (منفّذ في `NowBriefNotifier.kt`).
- **قناة الإشعار بأهمية `IMPORTANCE_DEFAULT` على الأقل** — قناة بأهمية
  `IMPORTANCE_LOW` لا تُرقّى أبداً إلى Live Update (هذا كان السبب الفعلي في
  عدم ظهور التطبيق إطلاقاً ضمن Settings > Notifications > Live notifications
  في الإصدار الأول من الكود، إلى جانب غياب `.setStyle()` بالكامل).
- **استخدام `.setStyle(NotificationCompat.BigTextStyle()...)` صراحة** —
  الإصدار الأول من الكود لم يستدعِ `setStyle()` مطلقاً، فلم يكن الإشعار
  مؤهلاً للترقية من الأساس بغض النظر عن أي إعداد آخر.

**شكل الكبسولة المتدرجة اللون التي تراها في لقطات الشاشة المرجعية (Now
brief / Evening brief) يرسمها النظام نفسه** من الأيقونة (`setSmallIcon`)
ولون التمييز (`setColor` + `setColorized`) — وليس من تخطيط نرسمه نحن، لأن
الترقية لا تقبل RemoteViews مخصصة كما ذُكر أعلاه. الشيء الوحيد الذي نرسمه
نحن بالكامل هو **ويدجت الشاشة الرئيسية** (`now_pill_layout.xml`)، وقد
أعدت تصميمه ليطابق نفس روح المرجع: شارة أيقونة دائرية شفافة + نص أبيض عريض
على تدرج لوني مشبّع لكل فترة.

## صفر استهلاك للبطارية
- لا توجد خدمة (Service) ولا حلقة `while(true)` ولا `WorkManager` دوري.
- `PeriodAlarmScheduler` يضبط **إنذاراً واحداً فقط** (`setExactAndAllowWhileIdle`)
  عند أقرب حد زمني قادم (06:00 / 12:00 / 17:00 / 20:00).
- عند إطلاقه، `PeriodAlarmReceiver` يحدّث الإشعار والويدجت، ثم يعيد جدولة
  الإنذار **التالي فقط**، وينام النظام مجدداً — 4 استيقاظات يومياً كحد أقصى.
- `BootReceiver` يعيد ضبط الإنذار بعد إعادة الإقلاع فقط (لأن AlarmManager
  يُفرَّغ عند إعادة التشغيل).
- الويدجت لا يحدد `updatePeriodMillis`، فلا يستطلعه النظام دورياً؛ يُحدَّث فقط
  من داخل `PeriodAlarmReceiver`.

## البناء عبر GitHub Actions
عند أي `push` إلى فرع `main` (أو تشغيل يدوي عبر تبويب Actions)، يقوم سير العمل
في `.github/workflows/build.yml` بـ:
1. تثبيت JDK 17 و Android SDK 36 و Gradle 8.9.
2. تنفيذ `gradle assembleRelease`.
3. رفع ملف الـ APK الناتج كـ **Artifact** قابل للتحميل مباشرة من صفحة
   التشغيل (Run) داخل تبويب **Actions**.

> ملاحظة: لم يتم تضمين `gradle-wrapper.jar` الثنائي (لا يمكن توليده بدون
> اتصال بالإنترنت في بيئة الإعداد هذه)، لذلك يستدعي سير العمل أمر `gradle`
> الذي يثبّته `setup-gradle` مباشرة بدل `./gradlew`. لتوليد الـ wrapper محلياً
> لاحقاً: `gradle wrapper --gradle-version 8.9`، ثم يمكنك تحويل الخطوة الأخيرة
> في `build.yml` إلى `./gradlew assembleRelease`.

## التشغيل محلياً
1. افتح المجلد في Android Studio (إصدار يدعم AGP 8.7 و SDK 36).
2. زامن Gradle (Sync).
3. شغّل على جهاز/محاكي Android 16 لرؤية ترقية Live Update كاملة (على إصدارات
   أقل، الإشعار يعمل ويتحدث دون شريحة الترقية).
