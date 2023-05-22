package edu.licenta.uptconnect.view.activity

import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivityMaterialsBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.model.DocumentType

class MaterialsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivityMaterialsBinding
    private var uri: Uri? = null

    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""
    private lateinit var course: Course
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
        getExtrasFromIntent()
        setProfileDetails()
        initializeMenu(
            binding.drawerLayout,
            binding.navigationView,
            0
        )
        initializeButtons()
    }

    private fun setBinding() {
        binding = ActivityMaterialsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getExtrasFromIntent() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
        imageUrl = intent.getStringExtra("imageUrl").toString()
        studentName = intent.getStringExtra("studentName").toString()
        course = intent.getParcelableExtra("course")!!
    }

    private fun setProfileDetails() {
        Picasso.get().load(imageUrl).into(binding.profileImage)
        binding.usernameId.text = studentName
    }

    private fun initializeButtons() {

        val studentsDatabase = Firebase.firestore
        val studentDoc = studentsDatabase.collection("students").document(studentFirebaseId)
        studentDoc.get()
            .addOnSuccessListener { documentSnapshot ->
                if(documentSnapshot.get("YearLeader") == true) {
                    binding.uploadMaterialArea.visibility = View.VISIBLE
                } else {
                    binding.uploadMaterialArea.visibility = View.INVISIBLE
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "Error retrieving Student", exception)
            }

        binding.imageUpload.setOnClickListener {
            if (binding.documentName.text!!.isEmpty()) {
                Toast.makeText(this, "The document must have a name", Toast.LENGTH_SHORT).show()
            } else {
                selectDocument("image", DocumentType.IMAGE)
            }
        }

        binding.pdfUpload.setOnClickListener {
            if (binding.documentName.text!!.isEmpty()) {
                Toast.makeText(this, "The document must have a name", Toast.LENGTH_SHORT).show()
            } else {

                selectDocument("application/pdf", DocumentType.PDF)
            }
        }

        binding.docxUpload.setOnClickListener {
            if (binding.documentName.text!!.isEmpty()) {
                Toast.makeText(this, "The document must have a name", Toast.LENGTH_SHORT).show()
            } else {
                selectDocument("docx", DocumentType.DOCX)
            }
        }

        binding.imagesButton.setOnClickListener {
            val intent = Intent(this, SeeMaterialsActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("userId", studentFirebaseId)
            intent.putExtra("email", email)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            intent.putExtra("type", "images")
            startActivity(intent)
        }
        binding.documentsButton.setOnClickListener {
            val intent = Intent(this, SeeMaterialsActivity::class.java)
            intent.putExtra("course", course)
            intent.putExtra("userId", studentFirebaseId)
            intent.putExtra("email", email)
            intent.putExtra("imageUrl", imageUrl)
            intent.putExtra("studentName", studentName)
            intent.putExtra("type", "pdf/docx")
            startActivity(intent)
        }
    }

    private fun selectDocument(type: String, docType: DocumentType) {
        val intent = Intent()
        intent.action = Intent.ACTION_GET_CONTENT

        when (docType) {
            DocumentType.IMAGE -> {
                intent.type = "$type/"
                startActivityForResult(intent, DocumentType.IMAGE.type)
            }
            DocumentType.PDF -> {
                intent.type = type
                startActivityForResult(intent, DocumentType.PDF.type)

            }
            DocumentType.DOCX -> {
                intent.type = "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                startActivityForResult(intent, DocumentType.DOCX.type)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val documentName = binding.documentName.text.toString()

        if (resultCode == RESULT_OK) {
            dialog = ProgressDialog(this)
            dialog.setMessage("Uploading")
            dialog.show()
            uri = data?.data
            when (requestCode) {
                DocumentType.IMAGE.type -> {
                    upload(uri!!, DocumentType.IMAGE, documentName)
                }
                DocumentType.PDF.type -> {
                    upload(uri!!, DocumentType.PDF, "$documentName.pdf")
                }
                DocumentType.DOCX.type -> {
                    upload(uri!!, DocumentType.DOCX, "$documentName.docx")
                }
            }
        }
    }

    private fun upload(uri: Uri, documentType: DocumentType, fileName: String) {
        val storageReference = FirebaseStorage.getInstance()

        val reference: StorageReference = when (documentType) {
            DocumentType.IMAGE -> {
                storageReference.getReference("${course.id}/images/$fileName")
            }
            DocumentType.PDF -> {
                storageReference.getReference("${course.id}/pdfs/$fileName")
            }

            DocumentType.DOCX -> {
                storageReference.getReference("${course.id}/docs/$fileName")
            }
        }
        reference.putFile(uri)
            .addOnCompleteListener {
                dialog.dismiss()
                Toast.makeText(this, "Document uploaded successfully", Toast.LENGTH_SHORT).show()
                binding.documentName.text?.clear()
            }
    }
}