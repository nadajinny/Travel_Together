package com.example.myapplication.ui.message

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R

// ChatAdapter: RecyclerView 어댑터 클래스
// 채팅 메시지 데이터를 관리하고 뷰에 표시하는 역할을 합니다.
// chatMessages: 채팅 메시지 데이터 리스트 (MutableList 형태)
class ChatAdapter(val context: Context, val chatMessages: MutableList<ChatModel>,val currentUserId: String)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    // addItem: 새로운 채팅 메시지를 리스트에 추가하는 함수
    // - item: ChatModel 데이터 객체 (새로 추가될 채팅 메시지)
    fun addItem(item: ChatModel) {
        // chatMessages가 null이 아닌 경우에만 메시지를 추가
        if (chatMessages != null) {
            chatMessages.add(item)
        }
    }

    // onCreateViewHolder: 뷰홀더 생성
    // - parent: RecyclerView의 부모 뷰그룹
    // - viewType: 뷰 타입 (1: 내 채팅, 2: 상대방 채팅)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view: View
        // viewType이 1이라면 내 채팅 레이아웃(item_mychat)을 inflate하고 Holder 생성
        if (viewType == 1) {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_mychat, parent, false)
            return Holder(view)
        }
        // viewType이 2라면 상대 채팅 레이아웃(item_youchat)을 inflate하고 Holder2 생성
        else {
            view = LayoutInflater.from(parent.context).inflate(R.layout.item_youchat, parent, false)
            return Holder2(view)
        }
    }
    // onBindViewHolder: 뷰홀더와 데이터를 바인딩
    // - viewHolder: 재사용 가능한 뷰홀더 객체
    // - i: 현재 데이터의 인덱스
    override fun onBindViewHolder(viewHolder: RecyclerView.ViewHolder, i: Int) {
        // Holder (내 채팅)인 경우
        if (viewHolder is Holder) {
            // 메시지 텍스트와 시간 정보를 내 채팅 레이아웃의 텍스트뷰에 설정
            (viewHolder as Holder).chat_Text?.setText(chatMessages[i].script)
            (viewHolder as Holder).chat_Time?.setText(chatMessages[i].date_time)
        }
        // Holder2 (상대 채팅)인 경우
        else if (viewHolder is Holder2) {
            // 상대방 이미지, 이름, 메시지 텍스트, 시간을 설정
            (viewHolder as Holder2).chat_You_Image?.setImageResource(R.mipmap.ic_launcher) // 상대방 이미지 (기본값 설정)
            (viewHolder as Holder2).chat_You_Name?.setText(chatMessages[i].name) // 상대방 이름
            (viewHolder as Holder2).chat_Text?.setText(chatMessages[i].script) // 메시지 텍스트
            (viewHolder as Holder2).chat_Time?.setText(chatMessages[i].date_time) // 메시지 시간
        }
    }
    // getItemCount: 데이터 리스트의 아이템 개수를 반환
    // RecyclerView가 몇 개의 아이템을 표시할지 결정
    override fun getItemCount(): Int {
        return chatMessages.size
    }

    // Holder: 내 채팅의 뷰홀더 클래스
    // - itemView: 내 채팅 레이아웃(item_mychat)의 뷰를 참조
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // chat_Text: 채팅 메시지를 표시하는 텍스트뷰
        // chat_Time: 메시지 시간을 표시하는 텍스트뷰
        val chat_Text = itemView?.findViewById<TextView>(R.id.chat_Text)
        val chat_Time = itemView?.findViewById<TextView>(R.id.chat_Time)
    }

    // Holder2: 상대 채팅의 뷰홀더 클래스
    // - itemView: 상대 채팅 레이아웃(item_youchat)의 뷰를 참조
    inner class Holder2(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // chat_You_Image: 상대방 이미지를 표시하는 이미지뷰
        // chat_You_Name: 상대방 이름을 표시하는 텍스트뷰
        // chat_Text: 채팅 메시지를 표시하는 텍스트뷰
        // chat_Time: 메시지 시간을 표시하는 텍스트뷰
        val chat_You_Image = itemView?.findViewById<ImageView>(R.id.chat_You_Image)
        val chat_You_Name = itemView?.findViewById<TextView>(R.id.chat_You_Name)
        val chat_Text = itemView?.findViewById<TextView>(R.id.chat_Text)
        val chat_Time = itemView?.findViewById<TextView>(R.id.chat_Time)
    }

    // getItemViewType: 특정 아이템의 뷰 타입을 반환
    // - position: 데이터 리스트의 인덱스
    override fun getItemViewType(position: Int): Int {

        Log.d("ChatAdapter", "currentUserId: $currentUserId, message name: ${chatMessages[position].name}")

        return if (chatMessages[position].name == currentUserId) {
            1 // 내 채팅
        } else {
            2 // 상대 채팅
        }
        return 1
    }
}
