package com.example.myapplication.ui.message

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.EditText
import android.widget.ListView
import android.widget.SimpleAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.R
import com.example.myapplication.data.model.SocketManager
import com.example.myapplication.databinding.FragmentMessageBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.json.JSONObject

class MessageFragment : Fragment() {

    private var _binding: FragmentMessageBinding? = null
    private val binding get() = _binding!!

    private lateinit var datas: MutableList<MutableMap<String, String>>
    private lateinit var simpleAdapter: SimpleAdapter
    private lateinit var button: FloatingActionButton

    // Firestore 인스턴스
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val messageViewModel = ViewModelProvider(this).get(MessageViewModel::class.java)
        _binding = FragmentMessageBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // 채팅방 리스트 초기화
        datas = mutableListOf()

        // SimpleAdapter 설정
        simpleAdapter = SimpleAdapter(
            requireContext(),
            datas,
            android.R.layout.simple_list_item_2,
            arrayOf("roomName"),
            intArrayOf(android.R.id.text1)
        )

        binding.lvChatlist.adapter = simpleAdapter

        // 리스트뷰 항목 클릭 리스너
        binding.lvChatlist.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            val selectedRoom = datas[position]["roomName"]
            val selectedRoomId = datas[position]["roomId"]

            val intent = Intent(requireContext(), ChatRoomActivity::class.java)
            intent.putExtra("roomName", selectedRoom)
            intent.putExtra("roomId", selectedRoomId)

            startActivity(intent)
        }

        // 리스트뷰 항목 길게 누르기 리스너 (삭제 기능)
        binding.lvChatlist.setOnItemLongClickListener { _, _, position, _ ->
            val selectedRoomId = datas[position]["roomId"]
            val selectedRoomName = datas[position]["roomName"]
            deleteRoom(selectedRoomId!!, selectedRoomName!!)
            true
        }

        // 채팅방 데이터 불러오기
        fetchChatRooms()

        // 채팅방 생성 버튼
        button = binding.btaddChatRoom
        button.setOnClickListener {
            showAddChatRoomDialog()
        }

        return root
    }

    private fun fetchChatRooms() {
        // Firestore에서 채팅방 데이터를 가져옴
        firestore.collection("chat_rooms")
            .orderBy("createdAt", Query.Direction.ASCENDING) // 생성 순서대로 정렬
            .get()
            .addOnSuccessListener { querySnapshot ->
                datas.clear() // 기존 데이터를 초기화
                for (document in querySnapshot.documents) {
                    val roomData = mutableMapOf(
                        "roomName" to (document["roomName"] as String),
                        "roomId" to document.id // Firestore 도큐먼트 ID를 roomId로 사용
                    )
                    datas.add(roomData)
                }
                simpleAdapter.notifyDataSetChanged() // 리스트뷰 갱신
            }
            .addOnFailureListener { exception ->
                Log.e("MessageFragment", "Error fetching chat rooms: ${exception.message}")
            }
    }

    private fun deleteRoom(roomId: String, roomName: String) {
        // Firestore에서 하위 컬렉션 및 문서 삭제
        val roomRef = firestore.collection("chat_rooms").document(roomId)
        val messagesCollection = roomRef.collection("messages")

        // 하위 컬렉션의 모든 문서 삭제
        messagesCollection.get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()
                for (doc in querySnapshot.documents) {
                    batch.delete(doc.reference)
                }

                // 하위 컬렉션 삭제가 완료되면 방 정보 삭제
                batch.commit()
                    .addOnSuccessListener {
                        roomRef.delete()
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "\"$roomName\" 방과 채팅 내역이 삭제되었습니다.", Toast.LENGTH_SHORT).show()
                                datas.removeIf { it["roomId"] == roomId } // 리스트에서 해당 항목 삭제
                                simpleAdapter.notifyDataSetChanged() // 리스트뷰 갱신
                            }
                            .addOnFailureListener { exception ->
                                Log.e("MessageFragment", "Error deleting room: ${exception.message}")
                                Toast.makeText(requireContext(), "방 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("MessageFragment", "Error deleting messages: ${exception.message}")
                        Toast.makeText(requireContext(), "채팅 내역 삭제에 실패했습니다.", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { exception ->
                Log.e("MessageFragment", "Error fetching messages: ${exception.message}")
                Toast.makeText(requireContext(), "채팅 내역을 불러오는 데 실패했습니다.", Toast.LENGTH_SHORT).show()
            }
    }


    private fun showAddChatRoomDialog() {
        // 다이얼로그 생성
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_chat_room, null)
        val editRoomName = dialogView.findViewById<EditText>(R.id.editRoomName)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("새 채팅방 생성")
            .setView(dialogView)
            .setPositiveButton("생성") { _, _ ->
                val roomName = editRoomName.text.toString().trim()
                if (roomName.isEmpty()) {
                    Toast.makeText(requireContext(), "방 이름을 입력하세요.", Toast.LENGTH_SHORT).show()
                } else {
                    createRoom(roomName)
                }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    private fun createRoom(roomName: String) {
        val roomData = JSONObject().apply {
            put("roomName", roomName)
            put("createdBy", "User") // 사용자 정보 추가
        }

        // 소켓을 통해 방 생성 요청 전송
        SocketManager.emit("create_room", roomData)

        // 방 생성 후 소켓 이벤트 리스너에서 처리
        SocketManager.on("room_created") { args ->
            requireActivity().runOnUiThread {
                val data = args[0] as JSONObject
                val newRoom = mutableMapOf(
                    "roomName" to data.getString("roomName"),
                    "roomId" to data.getString("roomId")
                )
                datas.add(newRoom)
                simpleAdapter.notifyDataSetChanged()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
