
import com.enfotrix.lifechanger.Models.TransactionModel

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.gson.Gson

data class User(
    var cnic: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var address: String = "",
    var phone: String = "",
    var status: String = "",
    var photo: String = "",
    var cnic_front: String = "",
    var cnic_back: String = "",
    var pin: String = "",
    var id: String = "",
    var fa_id: String = "",
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp
) : Parcelable {

    // Parcelable implementation
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readParcelable(Timestamp::class.java.classLoader) ?: Timestamp.now()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(cnic)
        parcel.writeString(firstName)
        parcel.writeString(lastName)
        parcel.writeString(address)
        parcel.writeString(phone)
        parcel.writeString(status)
        parcel.writeString(photo)
        parcel.writeString(cnic_front)
        parcel.writeString(cnic_back)
        parcel.writeString(pin)
        parcel.writeString(id)
        parcel.writeString(fa_id)
        parcel.writeParcelable(createdAt, flags)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<User> = object : Parcelable.Creator<User> {
            override fun createFromParcel(parcel: Parcel): User {
                return User(parcel)
            }

            override fun newArray(size: Int): Array<User?> {
                return arrayOfNulls(size)
            }
        }

        // Serialization/deserialization functions
        fun fromString(user: String): User? {
            val gson = Gson()
            return try {
                gson.fromJson(user, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }
}
















/*
data class User(
    var cnic: String,
    var firstName: String,
    var lastName: String,
    var address: String,
    var phone: String,
    var status: String,
    var pin: String
)*/

/*
data class User @JvmOverloads constructor(
    var cnic: String = "",
    var firstName: String = "",
    var lastName: String = "",
    var address: String = "",
    var phone: String = "",
    var status: String = "",
    var photo: String = "",
    var cnic_front: String = "",
    var cnic_back: String = "",
    var pin: String = "",
    var id: String = "",
    var fa_id: String = "",
    val createdAt: Timestamp = Timestamp.now() // Creation timestamp
){

    override fun toString(): String {
        val gson = Gson()
        return gson.toJson(this)
    }

    companion object {
        fun fromString(user: String): User? {
            val gson = Gson()
            return try {
                gson.fromJson(user, User::class.java)
            } catch (e: Exception) {
                null
            }
        }
    }
}*/
