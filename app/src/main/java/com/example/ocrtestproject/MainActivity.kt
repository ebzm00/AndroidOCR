package com.example.ocrtestproject


import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.app.ActivityCompat
import java.io.FileOutputStream
import java.text.SimpleDateFormat


class OpenCVActivity : Application() {

}

class MainActivity : AppCompatActivity() {
    
    //storage 권한 처리에 필요한 변수
    val CAMERA = arrayOf(Manifest.permission.CAMERA)
    val STORAGE = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.WRITE_EXTERNAL_STORAGE)
    val CAMERA_CODE = 98
    val STORAGE_CODE = 99



    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //카메라
        val camera = findViewById<Button>(R.id.camera)
        camera.setOnClickListener{
            CallCamera()
        }

        //사진 저장
        val picture = findViewById<Button>(R.id.picture)
        picture.setOnClickListener{
            GetAlbum()
        }

    }



    //카메라 권한, 저장소 권한
    //요청 권한
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            CAMERA_CODE -> {
                for (grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this,"카메라 권한을 승인해 주세요.",Toast.LENGTH_LONG).show()
                    }
                }
            }
            STORAGE_CODE -> {
                for (grant in grantResults){
                    if(grant != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(this,"저장소 권한을 승인해 주세요.",Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    //다른 권한등도 확인이 가능하도록
    fun checkPermission(permissions:Array<out String>, type:Int):Boolean{
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (permission in permissions) {
                if(ContextCompat.checkSelfPermission(this,permission)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this,permissions,type)
                    return false
                }
            }
        }
        return true
    }

    //카메라 촬영 - 권한 처리
    fun CallCamera() {
        if(checkPermission(CAMERA,CAMERA_CODE) && checkPermission(STORAGE,STORAGE_CODE)) {
            val itt = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(itt, CAMERA_CODE, null)
        }
    }

    // 사진 저장
    fun savefile(fileName:String, mimeType:String, bitmap: Bitmap):Uri?{

        var CV = ContentValues()

        //MediaStore 에  파일명, mimeType을 지정
        CV.put(MediaStore.Images.Media.DISPLAY_NAME,fileName)
        CV.put(MediaStore.Images.Media.MIME_TYPE, mimeType)

        //안정성 검사
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            CV.put(MediaStore.Images.Media.IS_PENDING, 1)
        }

        //MediaStore 에 파일을 저장
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, CV)
        if(uri != null) {
            var scriptor = contentResolver.openFileDescriptor(uri,"w")

            val fos = FileOutputStream(scriptor?.fileDescriptor)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.close()

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                CV.clear()
                //IS_PENDING을 초기화
                CV.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(uri,CV,null,null)
            }
        }
        return uri
    }
    //결과
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val imageView = findViewById<ImageView>(R.id.avatars)
//        //이미지뷰에서 비트맵 가져오기
//        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
//        // 비트맵을 Mat 형식으로 변환
//        val mat = Mat()
//        Utils.bitmapToMat(bitmap,mat)

        if (resultCode == Activity.RESULT_OK) {
            when(requestCode) {
                CAMERA_CODE -> {
                    if(data?.extras?.get("data") != null) {
                        val img = data?.extras?.get("data") as Bitmap
                        val uri = savefile(RandomFileName(), "image/jpeg", img)
                        imageView.setImageURI(uri)

                    }
                }
                STORAGE_CODE -> {
                    var uri = data?.data
                    imageView.setImageURI(uri)
                }
            }
        }
    }

    // 파일명을 날짜 저장
    fun RandomFileName() : String{
        val fileName = SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis())
        return fileName
    }
    
    // 갤러리 취득
    fun GetAlbum() {
        if(checkPermission(STORAGE,STORAGE_CODE)) {
            val itt = Intent(Intent.ACTION_PICK)
            itt.type = MediaStore.Images.Media.CONTENT_TYPE
            startActivityForResult(itt,STORAGE_CODE)
        }
      }
    }
