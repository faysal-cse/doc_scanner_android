package com.docscanner.app

import android.app.Application
import com.docscanner.app.data.DocumentStore

class DocScannerApplication : Application() {
    val documentStore: DocumentStore by lazy { DocumentStore(this) }
}
