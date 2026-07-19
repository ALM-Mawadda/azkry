package com.azkry.app.features.settings.viewmodels

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.azkry.app.R
import com.azkry.app.core.i18n.StringProvider
import com.azkry.app.features.settings.services.BackupService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val backupService: BackupService,
    private val strings: StringProvider,
) : ViewModel() {
    private val internalMessages = MutableSharedFlow<String>()

    /** One-shot messages (backup results) for a snackbar. */
    val messages: SharedFlow<String> = internalMessages

    fun onExportBackup(target: Uri) {
        viewModelScope.launch {
            val succeeded = try {
                val payload = backupService.exportJson()
                withContext(Dispatchers.IO) {
                    context.contentResolver.openOutputStream(target)?.use { stream ->
                        stream.write(payload.toByteArray())
                    } ?: error("stream unavailable")
                }
                true
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                false
            }
            internalMessages.emit(
                strings.get(
                    if (succeeded) R.string.backup_export_done else R.string.backup_failed,
                ),
            )
        }
    }

    fun onImportBackup(source: Uri) {
        viewModelScope.launch {
            val imported = try {
                val text = withContext(Dispatchers.IO) {
                    context.contentResolver.openInputStream(source)?.use { stream ->
                        stream.bufferedReader().readText()
                    } ?: error("stream unavailable")
                }
                backupService.importJson(text)
            } catch (error: CancellationException) {
                throw error
            } catch (_: Throwable) {
                false
            }
            internalMessages.emit(
                strings.get(
                    if (imported) R.string.backup_import_done else R.string.backup_failed,
                ),
            )
        }
    }
}
