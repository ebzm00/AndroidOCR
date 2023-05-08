package com.example.opencvtest

import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.ocrtestproject.R
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Files
import java.nio.file.Paths

class OpenCVActivity : AppCompatActivity() {
    private lateinit var mTess: TessBaseAPI // Tess API reference
    private var datapath = "" // 언어 데이터가 있는 경로

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 데이터 경로
        datapath = filesDir.toString() + "/tesseract/"

        // 트레이닝 데이터가 카피되어 있는지 체크
        checkFile(File(datapath + "tessdata/"), "kor")
        checkFile(File(datapath + "tessdata/"), "eng")

        // Tesseract API 언어 세팅
        val lang = "kor+eng"

        // OCR 세팅
        mTess = TessBaseAPI()
        mTess.init(datapath, lang)

        // 문자 인식 진행
        processImage(BitmapFactory.decodeResource(resources, R.drawable.res))
    }

    private fun processImage(bitmap: Bitmap) {
        Toast.makeText(
            applicationContext,
            "이미지가 복잡할 경우 해석 시 많은 시간이 소요될 수도 있습니다.",
            Toast.LENGTH_LONG
        ).show()
        mTess.setImage(bitmap)
        val OCRresult = mTess.utF8Text
        val OCRTextView = findViewById<TextView>(R.id.OCRTextView)

        OCRTextView.text = OCRresult
    }

    private fun copyFiles(lang: String) {
        try {
            // location we want the file to be at
            val filepath = "$datapath/tessdata/$lang.traineddata"

            // get access to AssetManager
            val assetManager: AssetManager = assets

            // open byte streams for reading/writing
            val inStream: InputStream = assetManager.open("tessdata/$lang.traineddata")
            val outStream: OutputStream = Files.newOutputStream(Paths.get(filepath))

            // copy the file to the location specified by filepath
            val buffer = ByteArray(1024)
            var read: Int
            while (inStream.read(buffer).also { read = it } != -1) {
                outStream.write(buffer, 0, read)
            }
            outStream.flush()
            outStream.close()
            inStream.close()

        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    // 파일 존재 확인
    private fun checkFile(dir: File, lang: String) {
        // directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(lang)
        }
        // The directory exists, but there is no data file in it
        if (dir.exists()) {
            val datafilePath = "$datapath/tessdata/$lang.traineddata"
            val datafile = File(datafilePath)
            if (!datafile.exists()) {
                copyFiles(lang)
            }
        }
    }
}