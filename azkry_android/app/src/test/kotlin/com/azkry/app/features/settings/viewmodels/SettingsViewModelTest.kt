package com.azkry.app.features.settings.viewmodels

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import app.cash.turbine.test
import com.azkry.app.core.i18n.StringProvider
import com.azkry.app.features.settings.services.BackupService
import io.mockk.every
import io.mockk.mockk
import java.io.ByteArrayInputStream
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.yield
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `export cancellation does not become a failure message`() = runTest {
        val backupService = CancellingBackupService(cancelExport = true)
        val strings = RecordingStringProvider()
        val viewModel = SettingsViewModel(
            context = mockk(relaxed = true),
            backupService = backupService,
            strings = strings,
        )

        viewModel.messages.test {
            viewModel.onExportBackup(mockk())
            backupService.exportCalled.await()
            yield()

            expectNoEvents()
        }

        assertTrue(strings.requestedIds.isEmpty())
    }

    @Test
    fun `import cancellation does not become a failure message`() = runTest {
        val source = mockk<Uri>()
        val resolver = mockk<ContentResolver> {
            every { openInputStream(source) } returns ByteArrayInputStream("{}".toByteArray())
        }
        val context = mockk<Context> {
            every { contentResolver } returns resolver
        }
        val backupService = CancellingBackupService(cancelImport = true)
        val strings = RecordingStringProvider()
        val viewModel = SettingsViewModel(context, backupService, strings)

        viewModel.messages.test {
            viewModel.onImportBackup(source)
            backupService.importCalled.await()
            yield()

            expectNoEvents()
        }

        assertTrue(strings.requestedIds.isEmpty())
    }
}

private class CancellingBackupService(
    private val cancelExport: Boolean = false,
    private val cancelImport: Boolean = false,
) : BackupService {
    val exportCalled = CompletableDeferred<Unit>()
    val importCalled = CompletableDeferred<Unit>()

    override suspend fun exportJson(): String {
        exportCalled.complete(Unit)
        if (cancelExport) throw CancellationException("export cancelled")
        return "{}"
    }

    override suspend fun importJson(json: String): Boolean {
        importCalled.complete(Unit)
        if (cancelImport) throw CancellationException("import cancelled")
        return true
    }
}

private class RecordingStringProvider : StringProvider {
    val requestedIds = mutableListOf<Int>()

    override fun get(resId: Int, vararg formatArgs: Any): String {
        requestedIds += resId
        return resId.toString()
    }
}
