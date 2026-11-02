package cn.fuckhome.xiaowine.utils

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class BackupUtilsTest {

    private val backupFile = File("src/test/resources/backup.json")

    @Test
    fun parseRealBackupFile() {
        val backup = BackupUtils.parseBackupJson(backupFile.readText())
        assertEquals(true, backup["MemoryView"])
        assertEquals(true, backup["MainSwitch"])
        assertEquals(true, backup["ZarmView"])
        assertEquals(true, backup["StorageView"])
        assertEquals(true, backup["Uptime"])
        assertEquals(true, backup["RunningAppTotal"])
        assertEquals(true, backup["RunningServiceTotal"])
        assertEquals(false, backup["Warning"])
        assertEquals(false, backup["Debug"])
        assertEquals(false, backup["CleanMode"])
        assertEquals(false, backup["hLauncherIcon"])
        assertEquals(true, backup["Unit"])
        assertEquals(8388613, backup["Gravity"])
        assertEquals("#000000", backup["Color"])
        assertEquals(47, backup["LeftMargin0"])
        assertEquals(75, backup["LeftMargin1"])
        // 旧版备份中已删除功能的键也应被解析（恢复后无害）
        assertTrue(backup.containsKey("Pad"))
        assertTrue(backup.size >= 25)
    }

    @Test
    fun parseFloatValueFromLegacyFormat() {
        val backup = BackupUtils.parseBackupJson("""{"BgCorners":"Float:30000"}""")
        assertEquals(30f, backup["BgCorners"])
    }

    @Test
    fun parsePlainStringValue() {
        val backup = BackupUtils.parseBackupJson("""{"Color":"#C0C0C0"}""")
        assertEquals("#C0C0C0", backup["Color"])
    }

    @Test
    fun parseToleratesBomAndWhitespace() {
        val backup = BackupUtils.parseBackupJson("\uFEFF  {\"MainSwitch\":true}\n")
        assertEquals(true, backup["MainSwitch"])
    }
}
