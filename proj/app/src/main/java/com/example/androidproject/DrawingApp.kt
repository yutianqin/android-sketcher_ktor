package com.example.androidproject

import android.app.Application
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class DrawingApp : Application()
{
    val scope = CoroutineScope(SupervisorJob())
    val db by lazy { DrawingDatabase.getDatabase(applicationContext) }
    val DrawingRepository by lazy { DrawingRepository(scope, db.drawingInfoDao()) }
}