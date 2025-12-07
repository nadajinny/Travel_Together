const express = require('express');
const http = require('http');
const { Server } = require('socket.io');

// Express 앱 초기화
const app = express();
const server = http.createServer(app);

// Socket.IO 초기화 및 CORS 설정
const io = new Server(server, {
  cors: {
    origin: "*", // 모든 도메인 허용
    methods: ["GET", "POST"]
  }
});

// 테스트용 라우트
app.get('/', (req, res) => {
  res.send('Socket.IO Server is running');
});

// 클라이언트 연결 처리
io.on('connection', (socket) => {
  console.log(`A user connected: ${socket.id}`);

  // 클라이언트에게 소켓 ID 전송
  socket.emit('socket_id', { socketId: socket.id });

  // 'chat_message' 이벤트 처리
  socket.on('chat_message', (msg) => {
    try {
      const message = typeof msg === 'string' ? JSON.parse(msg) : msg;

      console.log('Message received:', message);
      console.log(`Name: ${message.name}`);
      console.log(`Script: ${message.script}`);
      console.log(`Room Name: ${message.roomName}`);

      // 메시지 브로드캐스트
      io.emit('chat_message', message);
    } catch (err) {
      console.error('Error processing message:', err);
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
