package background.location.service.td

import android.content.Context
import android.util.Log
import background.location.service.MyApp
import background.location.service.MyUpdateWorker
import com.google.firebase.database.*
import java.util.*

class FirebaseHelperUtilz {
    companion object {
        var mInstance: FirebaseHelperUtilz? = null
        var database: FirebaseDatabase? = null
        var myRef: DatabaseReference? = null
        fun getInstance(/*mContext: Context*/): FirebaseHelperUtilz {
            if (mInstance == null) {
                mInstance = FirebaseHelperUtilz()
                database = FirebaseDatabase.getInstance();
                myRef = database!!.getReference(MyUpdateWorker.getDeviceName())
            }
            return mInstance!!;
        }
    }

    fun updateLocation(lat: String, lng: String) {
        val map = HashMap<String, String>()
        map[Date().toString() + ""] = lat + "," + lng

        myRef!!.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val post = snapshot.value
                if (post == null) {
                    myRef!!.setValue(map).addOnCompleteListener {
                        Log.d(
                            "keyss",
                            "Completed Update==>"
                        )
                    }.addOnFailureListener {
                        Log.d("keyss", "Failed update......")
                        ConnectivityChecker.checkInternet(MyApp.mContext!!) }
                } else {
                    val parentMap = post as HashMap<String, String>
                    parentMap.putAll(map)
                    myRef!!.setValue(parentMap).addOnCompleteListener {
                        Log.d(
                            "keyss",
                            "Completed Update==>"
                        )
                    }.addOnFailureListener {
                        Log.d("keyss", "Failed update......")
                        ConnectivityChecker.checkInternet(MyApp.mContext!!)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.w("keyss", "loadPost:onCancelled", error.toException())
                ConnectivityChecker.checkInternet(MyApp.mContext!!)
            }
        })
    }


}