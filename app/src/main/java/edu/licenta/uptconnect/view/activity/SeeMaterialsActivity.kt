package edu.licenta.uptconnect.view.activity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ListResult
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import edu.licenta.uptconnect.databinding.ActivitySeeMaterialsBinding
import edu.licenta.uptconnect.model.Course
import edu.licenta.uptconnect.model.Document
import edu.licenta.uptconnect.model.FileType
import edu.licenta.uptconnect.view.adapter.DocumentAdapter
import edu.licenta.uptconnect.view.adapter.ImageAdapter

class SeeMaterialsActivity : DrawerLayoutActivity() {

    private lateinit var binding: ActivitySeeMaterialsBinding

    private lateinit var course: Course
    private var studentFirebaseId = ""
    private var email = ""
    private var imageUrl: String = ""
    private var studentName: String = ""
    private var documentType: String = ""

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
        seeFiles()
    }

    private fun setBinding() {
        binding = ActivitySeeMaterialsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
    }

    private fun getExtrasFromIntent() {
        email = intent.getStringExtra("email").toString()
        studentFirebaseId = intent.getStringExtra("userId").toString()
        imageUrl = intent.getStringExtra("imageUrl").toString()
        studentName = intent.getStringExtra("studentName").toString()
        course = intent.getParcelableExtra("course")!!
        documentType = intent.getStringExtra("type").toString()
    }

    private fun setProfileDetails() {
        Picasso.get().load(imageUrl).into(binding.profileImage)
        binding.usernameId.text = studentName
    }

    private fun seeFiles() {
        val imageList: ArrayList<Document> = ArrayList()
        val progressBar = binding.progressBar
        val recyclerView = binding.documentsContent
        progressBar.visibility = View.VISIBLE

        if (documentType == "images") {
            val storageRef = FirebaseStorage.getInstance().reference.child("${course.id}/images/")
            val listAllTask: Task<ListResult> = storageRef.listAll()
            listAllTask.addOnCompleteListener { result ->
                val items: List<StorageReference> = result.result!!.items
                if (items.isEmpty()) {
                    binding.viewForNoImages.visibility = View.VISIBLE
                    progressBar.visibility = View.GONE
                    recyclerView.visibility = View.GONE
                } else {
                    items.forEachIndexed { _, item ->
                        val documentName = item.name
                        item.downloadUrl.addOnSuccessListener {
                            imageList.add(
                                Document(
                                    it.toString(),
                                    documentName,
                                    fileType = FileType.UNKNOWN
                                )
                            )
                        }.addOnCompleteListener {
                            recyclerView.adapter = ImageAdapter(imageList)
                            recyclerView.layoutManager = LinearLayoutManager(this)
                            recyclerView.visibility = View.VISIBLE
                            progressBar.visibility = View.INVISIBLE
                            binding.viewForNoImages.visibility = View.GONE
                        }
                    }
                }
            }
        } else {
            val paths = arrayOf("${course.id}/pdfs/", "${course.id}/docs/")
            for (path in paths) {
                val storageRefPdf = FirebaseStorage.getInstance().reference.child(path)
                val listAllTask: Task<ListResult> = storageRefPdf.listAll()
                listAllTask.addOnCompleteListener { result ->
                    val items: List<StorageReference> = result.result!!.items
                    if (items.isEmpty()) {
                        binding.viewForNoImages.visibility = View.VISIBLE
                        binding.textId.text = "There are no documents available"
                        progressBar.visibility = View.GONE
                        recyclerView.visibility = View.GONE
                    } else {
                        val downloadUrlTasks = ArrayList<Task<Uri>>()
                        items.forEachIndexed { _, item ->
                            val downloadUrlTask = item.downloadUrl
                            downloadUrlTasks.add(downloadUrlTask)

                            val documentName = item.name
                            val fileType = determineFileType(documentName)
                            downloadUrlTask.addOnSuccessListener {
                                imageList.add(Document(it.toString(), documentName, fileType))
                            }
                            Tasks.whenAllComplete(downloadUrlTask).addOnCompleteListener {
                                val adapter = DocumentAdapter(imageList)
                                adapter.onItemClick = { document ->
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setDataAndType(
                                        Uri.parse(document.documentUrl),
                                        getMimeType(document.fileType)
                                    )
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

                                    try {
                                        startActivity(intent)
                                    } catch (e: ActivityNotFoundException) {
                                    }
                                }
                                recyclerView.adapter = adapter
                                recyclerView.layoutManager = LinearLayoutManager(this)
                                recyclerView.visibility = View.VISIBLE
                                progressBar.visibility = View.INVISIBLE
                                binding.viewForNoImages.visibility = View.GONE
                            }
                        }
                    }
                }
            }
        }
    }

    private fun determineFileType(documentName: String): FileType {
        val lowercaseName = documentName.toLowerCase()

        return when {
            lowercaseName.endsWith(".pdf") -> FileType.PDF
            lowercaseName.endsWith(".docx") -> FileType.DOCX
            else -> FileType.UNKNOWN // Handle other file types or set a default value
        }
    }

    private fun getMimeType(fileType: FileType): String {
        return when (fileType) {
            FileType.PDF -> "application/pdf"
            FileType.DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            else -> "*/*" // Fallback to any MIME type if the file type is unknown
        }
    }
}