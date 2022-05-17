package background.location.service

import android.app.Application
import android.view.View

public class MyApp : Application() {
    companion object{
        var mView:View?=null
    }
    override fun onCreate() {
        super.onCreate()
    }
}