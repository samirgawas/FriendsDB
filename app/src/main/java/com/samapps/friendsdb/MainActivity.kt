package com.samapps.friendsdb

import android.os.Bundle
import android.text.TextUtils
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    var firebaseDatabase: FirebaseDatabase? = null
    var databaseReference: DatabaseReference? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        firebaseDatabase = FirebaseDatabase.getInstance()
//        databaseReference = FirebaseDatabase.getInstance().getReference("FriendDB")

        databaseReference = firebaseDatabase!!.reference.child("Users")

        btn_add_friend.setOnClickListener {


            if(validate().first){
                validate().second?.let { addFriendToFirebase(it) }
            }



        }

    }

    private fun validate() : Pair<Boolean, Friend?>{
        val name = name_Edit.text.toString()
        val radioGroup = gender_radio_group.checkedRadioButtonId
        val phone = mobile_edit.text.toString()
        val address = address_edit.text.toString()

        val radioValue: String = if(radioGroup != -1){
            val radio : RadioButton = findViewById(radioGroup)
            radio.text.toString()
        }else{
            ""
        }

        if(TextUtils.isEmpty(name)){
            Toast.makeText(this, "Please Enter Friend Name", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }
        if(radioGroup == -1){
            Toast.makeText(this, "Please Select Gender", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }
        if(TextUtils.isEmpty(phone)){
            Toast.makeText(this, "Please Enter Mobile Number", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }
        if(TextUtils.isEmpty(address)){
            Toast.makeText(this, "Please Enter Address", Toast.LENGTH_LONG).show()
            return Pair(false, null)
        }

        val friend = Friend(name = name, gender = radioValue, mobile = phone, address = address)

        return Pair(true, friend)

    }

    private fun addFriendToFirebase(friend : Friend){
        val key = databaseReference?.push()?.key

        key?.let {
            databaseReference?.child(it)?.setValue(friend).also {
                Toast.makeText(this@MainActivity, "data added", Toast.LENGTH_SHORT).show()
            }
        }
    }
}