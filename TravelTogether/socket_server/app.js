const express = require('express');
const http = require('http');
const { Server } = require('socket.io');
const admin = require('firebase-admin');

// Firebase Admin SDK 초기화
const serviceAccount = require('./ServiceKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
});
const db = admin.firestore(); // Firestore 데이터베이스

// Express 앱 초기화
const app = express();
const server = http.createServer(app);

// Socket.IO 초기화 및 CORS 설정
const io = new Server(server, {
  cors: {
    origin: "*", // 모든 도메인 허용
    methods: ["GET", "POST"],
  },
});

// 테스트용 라우트
app.get('/', (req, res) => {
  res.send('Socket.IO Server is running');
});

// 클라이언트 연결 처리
io.on('connection', (socket) => {
  console.log(`A user connected: ${socket.id}`);

  // 소켓 ID를 클라이언트에게 전송
  socket.emit('socket_id', { socketId: socket.id });

  // 채팅방 생성
  socket.on('create_room', async (roomData) => {
    try {
      const room = typeof roomData === 'string' ? JSON.parse(roomData) : roomData;
      const { roomName, createdBy } = room;

      // Firestore에서 방 이름이 이미 존재하는지 확인
      const existingRoom = await db
        .collection('chat_rooms')
        .where('roomName', '==', roomName)
        .limit(1) // 불필요한 데이터 로드 방지
        .get();

      if (!existingRoom.empty) {
        socket.emit('error', { message: 'Room name already exists' });
        return;
      }

      console.log(`Room created: ${roomName}, by: ${createdBy}`);

      // Firestore에 채팅방 정보 저장
      const roomRef = await db.collection('chat_rooms').add({
        roomName,
        createdBy,
        createdAt: admin.firestore.FieldValue.serverTimestamp(),
      });

      // 방 ID를 클라이언트로 전송
      socket.emit('room_created', { roomId: roomRef.id, roomName });
    } catch (err) {
      console.error('Error creating room:', err);
      socket.emit('error', { message: 'Error creating room' });
    }
  });

  // 채팅 메시지 처리
  socket.on('chat_message', async (msgData) => {
    try {
      const message = typeof msgData === 'string' ? JSON.parse(msgData) : msgData;
      const { roomId, name, script,date_time } = message;

      console.log(`Message received in room ${roomId}: ${name} - ${script}`);
      console.log('Message received:', message);


      // Firestore에 메시지 저장
      const messageRef = await db
        .collection('chat_rooms')
        .doc(roomId)
        .collection('messages')
        .add({
          name,
          script,
          timestamp: date_time,
        });

      // 저장된 메시지와 함께 클라이언트에 전송
      const savedMessage = (await messageRef.get()).data();
      //savedMessage의 데이터를 해체하여 클라이언트가 바로 키 값으로 접근
      //io.to(roomId).emit('chat_message', { ...savedMessage, roomId });
      // 실시간 보내면 되므로 굳이 위 코드처럼 방에 대한 메세지 보낼 필요 없음
      io.emit('chat_message', message);
    } catch (err) {
      console.error('Error processing message:', err);
      socket.emit('error', { message: 'Error sending message' });
    }
  });

  // 방에 참여
  socket.on('join_room', async (roomId) => {
    try {
      console.log(`User joined room: ${roomId}`);
      socket.join(roomId);

      // Firestore에서 기존 메시지 가져오기
      const messagesSnapshot = await db
        .collection('chat_rooms')
        .doc(roomId)
        .collection('messages')
        .orderBy('timestamp')
        .get();

      const messages = messagesSnapshot.docs.map((doc) => doc.data());

      // 기존 메시지를 클라이언트에 전송
      socket.emit('room_messages', { roomId, messages });
    } catch (err) {
      console.error('Error joining room:', err);
      socket.emit('error', { message: 'Error joining room' });
    }
  });

  // 연결 해제 처리
  socket.on('disconnect', () => {
    console.log(`A user disconnected: ${socket.id}`);
  });
});

// 서버 실행
const PORT = 3005;
server.listen(PORT, () => {
  console.log(`Server is running on http://localhost:${PORT}`);
});
