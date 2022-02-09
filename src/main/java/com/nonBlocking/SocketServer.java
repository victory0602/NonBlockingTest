package com.nonBlocking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
* SocketServer의 Blocking, NonBlocking 예시이다.
* Socket통신은 양방향 통신이기 때문에 요청을 하는 SocketServer가 Blocking Server라면
* 요청을 받는 Socket Server도 Blocking Server로 해야한다.
* NonBlocking Server라면 똑같이 NonBlocking Server로 만들어준다.
* */
public class SocketServer {
    private static Logger log = LoggerFactory.getLogger(SocketServer.class);

    public static void main(String[] args) {
        // Blocking
        /*try {
            // ServerSocketChannel을 생성하고, 몇 가지 설정을 한다.
            ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(true);
            serverSocketChannel.bind(new InetSocketAddress(28080));

            while (true) {
                // 블로킹 방식으로 설정했기 때문에, accept() 메서드에서 연결을 받을 때까지 블락된다.
                SocketChannel socketChannel = serverSocketChannel.accept();

                // 연결된 클라이언트와 입/출력하기.
                Charset charset = Charset.forName("UTF-8");

                ByteBuffer byteBuffer = ByteBuffer.allocate(128);

                socketChannel.read(byteBuffer);

                byteBuffer.flip();
                log.info("Received Data : " + charset.decode(byteBuffer).toString());

                byteBuffer = charset.encode("http://localhost:8080 - Socket Call Test.");
                Thread.sleep(2000);
                socketChannel.write(byteBuffer);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }*/
        /* NonBlocking
        public class SocketServerApplication {
            private static Logger log = LoggerFactory.getLogger(SocketServerApplication.class);
            ServerSocketChannel serverSocketChannel = null;
            public static void main(String[] args) {
                Server server = new Server();
                new Thread(server).start();
            }

            public static class Server implements Runnable {
                public void run() {
                    try (Selector selector = Selector.open()) {
                        try (ServerSocketChannel serverChannel = ServerSocketChannel.open()) {
                            serverChannel.configureBlocking(true);
                            serverChannel.socket().bind(new InetSocketAddress(28080));
                            serverChannel.register(selector, SelectionKey.OP_ACCEPT);
                            while (selector.select() > 0) {
                                Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
                                while (keys.hasNext()) {
                                    SelectionKey key = keys.next();
                                    keys.remove();
                                    if (!key.isValid()) {
                                        continue;
                                    }
                                    if (key.isAcceptable()) {
                                        this.accept(selector, key);
                                    } else if (key.isReadable()) {
                                        this.receive(selector, key);
                                    } else if (key.isWritable()) {
                                        this.send(selector, key);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                public static void accept(Selector selector, SelectionKey selectionKey) {
                    try {
                        log.info("accept Start");
                        // 키 채널 가져오기
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

                        // 채널 가져오기
                        SocketChannel socketChannel = serverSocketChannel.accept();

                        // NonBlocking 설정
                        socketChannel.configureBlocking(false);

                        // 접속 Socket 단위로 사용되는 Buffer
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append("NonBlocking Socket Server. call accept");

                        // Socekt채널을 channel에 송신 등록
                        socketChannel.register(selector, SelectionKey.OP_WRITE, stringBuffer);
                        log.info("accept End");
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // 수신
                public static void receive(Selector selector, SelectionKey selectionKey) {
                    try {
                        log.info("receive Start");
                        // 키 채널 가져오기
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                        // NonBlocking 설정
                        socketChannel.configureBlocking(false);

                        ByteBuffer byteBuffer = ByteBuffer.allocate(128);

                        // 데이터 수신
                        int readSize = socketChannel.read(byteBuffer);
                        if(readSize == -1) {
                            socketChannel.close();
                            selectionKey.cancel();
                            return;
                        }

                        byte[] data = new byte[readSize];
                        System.arraycopy(byteBuffer.array(), 0, data, 0, readSize);

                        StringBuffer stringBuffer = (StringBuffer) selectionKey.attachment();
                        stringBuffer.append(new String(data));
                        log.info("receive stringBuffer : {}", stringBuffer.toString());

                        socketChannel.register(selector, SelectionKey.OP_WRITE, stringBuffer);
                        log.info("receive End");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // 발신
                public static void send(Selector selector, SelectionKey selectionKey) {
                    try {
                        log.info("send Start");
                        // 키 채널 가져오기
                        SocketChannel socketChannel = (SocketChannel) selectionKey.channel();

                        // NonBlocking 설정
                        socketChannel.configureBlocking(false);

                        StringBuffer stringBuffer = (StringBuffer) selectionKey.attachment();
                        String data = "NonBlocking Socket Server. call send";

                        stringBuffer.setLength(0);
                        ByteBuffer byteBuffer = ByteBuffer.wrap(data.getBytes());

                        Thread.sleep(2000);
                        socketChannel.write(byteBuffer);
                        socketChannel.register(selector, SelectionKey.OP_READ, stringBuffer);
                        log.info("send End");
                    } catch (IOException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

         */
    }
}
