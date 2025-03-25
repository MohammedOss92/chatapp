//package com.sarrawi
//
//fun getCurrentTime(): String {
//    val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//    return formatter.format(Date())
//}
//
//fun getCurrentDate(): String {
//    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//    return formatter.format(Date())
//}
//
//fun formatDate(timestamp: String): String {
//    return try {
//        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//        val date = Date(timestamp.toLong()) // تحويل النص إلى رقم ثم إلى كائن Date
//        formatter.format(date)
//    } catch (e: NumberFormatException) {
//        "Invalid timestamp" // تجنب حدوث خطأ عند تمرير قيمة غير صحيحة
//    }
//}
//الفرق بين الدوال الثلاثة:
//1️⃣ getCurrentTime()
//
//تعيد الوقت الحالي فقط بصيغة (HH:mm:ss) مثل: 14:30:15.
//
//تستخدم SimpleDateFormat للحصول على الوقت بدون التاريخ.
//
//2️⃣ getCurrentDate()
//
//تعيد التاريخ الحالي فقط بصيغة (yyyy-MM-dd) مثل: 2025-03-19.
//
//لا تعرض الوقت، فقط السنة والشهر واليوم.
//
//3️⃣ formatDate(timestamp: String)
//
//تأخذ طابعًا زمنيًا (timestamp) على شكل نص (String) يمثل عدد الميلي ثانية منذ 1970.
//
//تقوم بتحويل الطابع الزمني إلى تاريخ بصيغة (yyyy-MM-dd).
//
//إذا كانت القيمة المدخلة غير صحيحة، تُعيد "Invalid timestamp" بدلاً من حدوث خطأ.