package com.example.data.model

data class DoctorCourse(
    val id: String,
    val subjectName: String,
    val doctorName: String,
    val departmentCode: String,
    val yearGroup: Int,
    val doctorNationalId: String = "",
    val description: String = ""
)

object CourseCatalog {
    val defaultCourses = listOf(
        // MIS Year 1
        DoctorCourse("mis_1_1", "مقدمة في نظم المعلومات الإدارية", "أ.د. أحمد السعيد", "MIS", 1, description = "المفاهيم الأساسية لنظم المعلومات ودورها في المنظمات الحديثة"),
        DoctorCourse("mis_1_2", "مبادئ إدارة الأعمال والتنظيم", "د. محمود فتحي", "MIS", 1, description = "وظائف الإدارة: التخطيط، التنظيم، التوجيه، والرقابة"),
        DoctorCourse("mis_1_3", "أساسيات تكنولوجيا المعلومات والحاسب", "د. إبراهيم كمال", "MIS", 1, description = "مكونات الحاسب، أنظمة التشغيل، وبرمجيات التطبيقات المكتبية"),
        DoctorCourse("mis_1_4", "رياضيات الأعمال والإحصاء التطبيقي", "د. سامح عبد الفتاح", "MIS", 1, description = "الاحتمالات، التوزيعات الإحصائية، وتحليل بيانات الأعمال"),

        // MIS Year 2
        DoctorCourse("mis_2_1", "تحليل وتصميم النظم (Systems Analysis)", "أ.د. سامح عبد الفتاح", "MIS", 2, description = "دورة حياة تطوير النظم ومخططات التدفق DFD و UML"),
        DoctorCourse("mis_2_2", "قواعد البيانات وSQL", "أ.د. أحمد السعيد", "MIS", 2, description = "النموذج العلائقي، لغة SQL، وتطبيع الجداول Normalization"),
        DoctorCourse("mis_2_3", "شبكات الحاسب وأمن المعلومات", "د. إبراهيم كمال", "MIS", 2, description = "معمارية الشبكات LAN/WAN وبروتوكولات الإنترنت TCP/IP"),
        DoctorCourse("mis_2_4", "التجارة الإلكترونية والتسويق الرقمي", "د. رانيا الشناوي", "MIS", 2, description = "نماذج B2B و B2C واستراتيجيات المنصات الرقمية"),

        // MIS Year 3
        DoctorCourse("mis_3_1", "تخطيط موارد المؤسسات (ERP Systems)", "أ.د. سامح عبد الفتاح", "MIS", 3, description = "أنظمة SAP و Odoo وتكامل وظائف المنشأة وسلاسل الإمداد"),
        DoctorCourse("mis_3_2", "إدارة قواعد البيانات المتقدمة", "أ.د. أحمد السعيد", "MIS", 3, description = "فهرسة البيانات، استعلامات متقدمة، والنسخ الاحتياطي السحابي"),
        DoctorCourse("mis_3_3", "ذكاء الأعمال وتحليل البيانات (BI)", "د. إبراهيم كمال", "MIS", 3, description = "لوحات القيادة التفاعلية Dashboards ومستودعات البيانات Data Warehousing"),
        DoctorCourse("mis_3_4", "إدارة مشروعات نظم المعلومات (IT PM)", "د. رانيا الشناوي", "MIS", 3, description = "منهجيات Agile و Scrum وإدارة الجدول الزمني والمخاطر"),

        // MIS Year 4
        DoctorCourse("mis_4_1", "أمن المعلومات ونظم الدعم الذكية", "أ.د. سامح عبد الفتاح", "MIS", 4, description = "نظم دعم القرار DSS والتشفير والأمن السيبراني المؤسسي"),
        DoctorCourse("mis_4_2", "استراتيجيات تكنولوجيا المعلومات والحوكمة", "أ.د. أحمد السعيد", "MIS", 4, description = "حوكمة تقنية المعلومات COBIT والتوافق مع رؤية مصر 2030"),
        DoctorCourse("mis_4_3", "مشروع التخرج الأكاديمي وتطبيقات السحابة", "د. إبراهيم كمال", "MIS", 4, description = "تطوير مشروع تخرج متكامل مع الربط بقواعد بيانات سحابية"),

        // CS Year 1
        DoctorCourse("cs_1_1", "مقدمة في علوم الحاسب والبرمجة", "أ.د. عادل منصور", "CS", 1, description = "أساسيات لغات البرمجة وحل المشكلات التوافقي"),
        DoctorCourse("cs_1_2", "رياضيات الحاسب والمنطق الرقمي", "د. خالد العوضي", "CS", 1, description = "الجبر الخطي والمنطق الرياضي والبوابات المنطقية"),
        DoctorCourse("cs_1_3", "تراكيب محددة (Discrete Structures)", "د. منى زهران", "CS", 1, description = "المجموعات، العلاقات، والدوال ونظرية الرسوم البيانية"),

        // CS Year 2
        DoctorCourse("cs_2_1", "هياكل البيانات والخوارزميات (Data Structures)", "أ.د. عادل منصور", "CS", 2, description = "المصفوفات، القوائم المترابطة، الأشجار، والفرز والبحث"),
        DoctorCourse("cs_2_2", "برمجة كائنية التوجه (OOP)", "د. منى زهران", "CS", 2, description = "الوراثة، تعدد الأشكال، التغليف، وتصميم الكائنات"),
        DoctorCourse("cs_2_3", "معمارية الحاسب ولغة التجميع", "د. خالد العوضي", "CS", 2, description = "تصميم المعالج الدقيق ومسجلات الذاكرة"),

        // CS Year 3
        DoctorCourse("cs_3_1", "هندسة البرمجيات (Software Engineering)", "أ.د. عادل منصور", "CS", 3, description = "نماذج التطوير SDLC وهندسة المتطلبات واختبار البرمجيات"),
        DoctorCourse("cs_3_2", "الذكاء الاصطناعي وتعلم الآلة (AI & ML)", "د. خالد العوضي", "CS", 3, description = "خوارزميات التعلم الخاضع للإشراف والشبكات العصبية"),
        DoctorCourse("cs_3_3", "أنظمة التشغيل (Operating Systems)", "د. منى زهران", "CS", 3, description = "إدارة العمليات والذاكرة والملفات ونظم Linux"),

        // CS Year 4
        DoctorCourse("cs_4_1", "الحوسبة السحابية وأمن السيبراني", "أ.د. عادل منصور", "CS", 4, description = "خدمات AWS/R2 ونماذج السحاب وتأمين النظم"),
        DoctorCourse("cs_4_2", "معالجة اللغات الطبيعية والرؤية الحاسوبية", "د. خالد العوضي", "CS", 4, description = "تحليل النصوص الرقمية ومعالجة الصور المتقدمة"),
        DoctorCourse("cs_4_3", "مشروع التخرج في علوم الحاسب", "د. منى زهران", "CS", 4, description = "تنفيذ منظومة برمجية أو نموذج ذكاء اصطناعي تطبيقي"),

        // BA Year 1
        DoctorCourse("ba_1_1", "أصول الإدارة والقيادة المؤسسية", "أ.د. هاني رضوان", "BA", 1, description = "النظريات الإدارية الحديثة ومهارات القيادة"),
        DoctorCourse("ba_1_2", "مبادئ الاقتصاد الجزئي والكلي", "د. نهال الألفي", "BA", 1, description = "قوى العرض والطلب والمؤشرات الاقتصادية القومية"),
        DoctorCourse("ba_1_3", "مبادئ المحاسبة المالية", "د. طارق جودة", "BA", 1, description = "دورة المحاسبة، قيود اليومية، والقوائم المالية"),

        // BA Year 2
        DoctorCourse("ba_2_1", "إدارة التسويق وبحوث السوق", "د. نهال الألفي", "BA", 2, description = "المزيج التسويقي 4Ps وسلوك المستهلك والتسويق الإلكتروني"),
        DoctorCourse("ba_2_2", "إدارة الموارد البشرية (HRM)", "أ.د. هاني رضوان", "BA", 2, description = "التوظيف، التدريب، تقييم الأداء، والتعويضات"),
        DoctorCourse("ba_2_3", "التمويل والإدارة المالية", "د. طارق جودة", "BA", 2, description = "إدارة رأس المال العامل وتقييم الاستثمارات والتدفقات النقدية"),

        // BA Year 3
        DoctorCourse("ba_3_1", "الإدارة الاستراتيجية والتخطيط", "أ.د. هاني رضوان", "BA", 3, description = "تحليل SWOT وبناء الميزة التنافسية للشركات"),
        DoctorCourse("ba_3_2", "إدارة سلاسل الإمداد واللوجستيات", "د. طارق جودة", "BA", 3, description = "إدارة المخزون والتوريد والتوزيع الدولي"),
        DoctorCourse("ba_3_3", "التسويق الدولي وسلوك المستهلك", "د. نهال الألفي", "BA", 3, description = "استراتيجيات دخول الأسواق العالمية والعلامة التجارية"),

        // BA Year 4
        DoctorCourse("ba_4_1", "ريادة الأعمال وإدارة المشروعات الصغيرة", "أ.د. هاني رضوان", "BA", 4, description = "نموذج العمل التجاري BMC ودراسات الجدوى"),
        DoctorCourse("ba_4_2", "إدارة الجودة الشاملة (TQM)", "د. نهال الألفي", "BA", 4, description = "معايير ISO وتحسين العمليات المستمر"),
        DoctorCourse("ba_4_3", "مشروع التخرج في إدارة الأعمال", "د. طارق جودة", "BA", 4, description = "خطة استراتيجية متكاملة لشركة ناشئة أو تطوير منشأة")
    )

    fun getCoursesFor(deptCode: String, year: Int, existingLectures: List<Lecture>, existingAssignments: List<Assignment>): List<DoctorCourse> {
        val predefined = defaultCourses.filter { it.departmentCode.equals(deptCode, ignoreCase = true) && it.yearGroup == year }
        val predefinedSubjects = predefined.map { it.subjectName.trim().lowercase() }.toSet()

        // Also add any custom subjects added via Lectures or Assignments
        val customSubjects = mutableListOf<DoctorCourse>()
        val allContentSubjects = (existingLectures.map { it.subjectName to it.doctorName } + existingAssignments.map { it.subjectName to it.doctorName }).distinctBy { it.first.trim().lowercase() }

        allContentSubjects.forEach { (subj, doc) ->
            if (subj.trim().lowercase() !in predefinedSubjects && subj.isNotBlank()) {
                customSubjects.add(
                    DoctorCourse(
                        id = "custom_${subj.hashCode()}",
                        subjectName = subj.trim(),
                        doctorName = doc.ifBlank { "أستاذ المقرر" },
                        departmentCode = deptCode,
                        yearGroup = year,
                        description = "مقرر دراسي معتمد"
                    )
                )
            }
        }

        return predefined + customSubjects
    }
}
