package com.example.data.model

import com.example.util.FormatUtils

enum class LetterTemplateType(val code: String, val label: String) {
    UNDANGAN("UNDANGAN", "Surat Undangan Rapat / Kegiatan"),
    TUGAS("TUGAS", "Surat Tugas Delegasi Guru"),
    NARASUMBER("NARASUMBER", "Surat Permohonan Narasumber / Pemateri");

    companion object {
        fun fromCode(code: String): LetterTemplateType =
            entries.firstOrNull { it.code == code } ?: UNDANGAN
    }
}

data class KopSuratConfig(
    val kkgName: String = "KELOMPOK KERJA GURU PENDIDIKAN AGAMA ISLAM (KKG PAI)",
    val districtName: String = "KECAMATAN GEDEBAGE • KOTA BANDUNG",
    val address: String = "Sekretariat: KKG PAI Kecamatan Gedebage, Kota Bandung",
    val contactInfo: String = "Email: kkgpai.gedebage@gmail.com | Telp/WA: 0812-3456-7890",
    val logoBase64: String = "",
    val chairpersonName: String = "H. Ahmad Fauzi, S.Pd.I., M.Pd.",
    val chairpersonNip: String = "19780514 200501 1 006",
    val secretaryName: String = "Siti Nurhaliza, S.Ag.",
    val secretaryNip: String = "19840921 201001 2 012"
)

data class LetterDraft(
    val templateType: String = LetterTemplateType.UNDANGAN.code,
    val letterNumber: String = "014/KKG-PAI-GDB/XI/2026",
    val attachment: String = "1 (Satu) Berkas",
    val subject: String = "Undangan Rapat Koordinasi & Workshop Kurikulum Merdeka",
    val letterDate: String = FormatUtils.todayIndonesianFull(),
    val cityName: String = "Gedebage",
    val recipient: String = "Yth. Bapak/Ibu Guru PAI SD/MI Se-Kecamatan Gedebage\ndi Tempat",
    val eventDayDate: String = "Sabtu, 28 November 2026",
    val eventTime: String = "08.30 WIB s.d. 12.30 WIB",
    val eventLocation: String = "Aula Sekretariat KKG PAI Kecamatan Gedebage",
    val openingText: String = LetterTemplates.openingFor(LetterTemplateType.UNDANGAN),
    val closingText: String = LetterTemplates.closingFor(LetterTemplateType.UNDANGAN)
)

object LetterTemplates {
    fun subjectFor(type: LetterTemplateType): String = when (type) {
        LetterTemplateType.UNDANGAN -> "Undangan Rapat Koordinasi & Pembinaan KKG PAI Gedebage"
        LetterTemplateType.TUGAS -> "Surat Tugas Delegasi Kegiatan KKG PAI Gedebage"
        LetterTemplateType.NARASUMBER -> "Permohonan Kesediaan Menjadi Narasumber / Pemateri"
    }

    fun recipientFor(type: LetterTemplateType): String = when (type) {
        LetterTemplateType.UNDANGAN -> "Yth. Bapak/Ibu Guru PAI Se-Kecamatan Gedebage\ndi Tempat"
        LetterTemplateType.TUGAS -> "Yth. Kepala Sekolah SDN / SDIT Se-Kecamatan Gedebage\ndi Tempat"
        LetterTemplateType.NARASUMBER -> "Yth. Pengawas PAI / Narasumber Ahli\nKantor Kementerian Agama Kota Bandung"
    }

    fun openingFor(type: LetterTemplateType): String = when (type) {
        LetterTemplateType.UNDANGAN ->
            "Assalamu'alaikum Wr. Wb.\nDengan memohon rahmat dan ridho Allah SWT, serta dalam rangka meningkatkan kompetensi profesional dan sinergi Guru Pendidikan Agama Islam Kecamatan Gedebage, kami mengharap kehadiran Bapak/Ibu pada kegiatan yang insyaAllah akan dilaksanakan pada:"
        LetterTemplateType.TUGAS ->
            "Assalamu'alaikum Wr. Wb.\nBerdasarkan program kerja Kelompok Kerja Guru Pendidikan Agama Islam (KKG PAI) Kecamatan Gedebage serta hasil musyawarah pengurus, dengan ini kami menugaskan perwakilan Guru PAI untuk mengikuti kegiatan delegasi resmi yang akan dilaksanakan pada:"
        LetterTemplateType.NARASUMBER ->
            "Assalamu'alaikum Wr. Wb.\nDalam rangka peningkatan mutu pembelajaran dan penguatan moderasi beragama bagi Guru PAI Kecamatan Gedebage, kami bermaksud memohon kesediaan Bapak/Ibu untuk berkenan menjadi Narasumber / Pemateri pada kegiatan yang akan dilaksanakan pada:"
    }

    fun closingFor(type: LetterTemplateType): String = when (type) {
        LetterTemplateType.UNDANGAN ->
            "Mengingat pentingnya acara tersebut, besar harapan kami Bapak/Ibu dapat hadir tepat pada waktunya. Atas perhatian dan kehadirannya kami ucapkan jazakumullahu khairan katsiran.\nWassalamu'alaikum Wr. Wb."
        LetterTemplateType.TUGAS ->
            "Demikian surat tugas ini dibuat untuk dilaksanakan dengan penuh tanggung jawab serta melaporkan hasil kegiatan kepada pengurus KKG PAI Kecamatan Gedebage.\nWassalamu'alaikum Wr. Wb."
        LetterTemplateType.NARASUMBER ->
            "Besar harapan kami atas kesediaan dan dukungan Bapak/Ibu demi kelancaran serta keberkahan kegiatan ini. Atas perhatian dan perkenannya kami ucapkan terima kasih.\nWassalamu'alaikum Wr. Wb."
    }
}
