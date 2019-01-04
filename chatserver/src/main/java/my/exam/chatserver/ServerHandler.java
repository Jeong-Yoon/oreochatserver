package my.exam.chatserver;

import java.net.Socket;
import java.util.List;

public class ServerHandler implements Runnable {
    private Socket socket;
    private ChatLobby chatLobby;
    private boolean inRoom;

    public ServerHandler(Socket socket, ChatLobby chatLobby) {
        this.socket = socket;
        this.chatLobby = chatLobby;
        inRoom = false;

    }

    @Override
    public void run() {
        ChatUser chatUser = new ChatUser(socket);
        String nickname = chatUser.read();
        chatUser.setNickname(nickname);
        System.out.println("message : " + nickname);

        chatLobby.addChatUser(chatUser);

        try {
            while (true) {
                String message = chatUser.read();
                System.out.println(chatUser.getNickname() + "님의 입력 : " + message);
                /**
                 * create를 할 때 방 제목을 입력하지 않은 경우
                 * 띄어쓰기를 하지 않고 엔터를 누르면 방 제목이 /create로 들어가는 문제가 있습니다.
                 *
                 * join을 할 때 방 번호를 입력하지 않은 경우 프로그램이 죽는 문제를 해결해주세요.
                 *
                 * quit을 했을 때 클라이언트를 종료해주세요.
                 */
                if (!inRoom) { // 로비에 있을 경우
                    if (message.indexOf("/create") == 0) {
                        String title = message.substring(message.indexOf(" ") + 1);
                        chatLobby.createRoom(chatUser, title, true);
                        inRoom = true;
                        //방 생성 후 성공했다는 문구를 남겼습니다.
                        System.out.println("방이 생성되었습니다.");

                    }else if(message.indexOf("/join") == 0){
                        String strRoomNum = message.substring(message.indexOf(" ") +1);
                        System.out.println(strRoomNum);
                        int roomNum = Integer.parseInt(strRoomNum);
                        chatLobby.joinRoom(roomNum, chatUser);
                        inRoom = true;
                    } else if (message.indexOf("/roomlist") == 0) {
                        List<ChatRoom> chatRooms = chatLobby.getChatRooms();
                        int i = 0;
                        for (ChatRoom cr : chatRooms) {
                            chatUser.write(i + " : " + cr.getTitle());
                            i++;
                        }

                    } else if (message.indexOf("/quit") == 0) {
                        chatLobby.exit(chatUser);
                    }
                } else { // 방안에 있을 경우

                    /**
                     * 방에 입장 했을 때 알려주는 문구와 명령어를 알려주는 문구를 추가하면 좋을 것 같습니다.
                     *
                     * 방에서 나가는 메소드를 만들면 좋을 것 같습니다.
                     * 방에서 강퇴시키면 강퇴당한 유저가 방의 유저 list에서 제외되나요?
                     *
                     */
                    if (message.indexOf("/whisper") == 0) {
                        String[] s = message.split(" ");
                        String whisperTo = s[1];
                        System.out.println("whisper to: " + whisperTo);
                        String msg = message.substring(message.indexOf(s[2]));
                        List<ChatUser> chatUsers = chatLobby.getUser(chatUser);
                        for (ChatUser cu : chatUsers) {
                            if(cu.getNickname().equals(whisperTo)){
                                cu.write("[" + chatUser.getNickname()+ "님의 귓속말]" + msg);

                            }
                        }

                    } else if (message.indexOf("/kick") == 0) {
                        if (chatUser.roomMaster() == true) {
                            String kickWho = message.substring(message.indexOf(" ") + 1);
                            List<ChatUser> chatUsers = chatLobby.getUser(chatUser);
                            for (ChatUser cu : chatUsers) {
                                if (cu.getNickname().equals(kickWho)) {
                                    cu.close();
                                    // 방에서 사람을 강퇴시킨 다음에 방의 유저목록에서 사람을 삭제하는 메소드가 필요해보입니다.
                                }else if(chatUser.getNickname().equals(kickWho)){
                                    chatUser.write("자기자신은 강퇴시킬 수 없습니다.");
                                }else{
                                    chatUser.write(kickWho + " 해당하는 유저가 없습니다.");
                                }
                            }
                        } else {
                            chatUser.write("방장이 아닙니다.");
                        }
                    } else if(message.indexOf("/giveMaster") == 0){


                    } else if (message.indexOf("/change") == 0) {
                        String name = message.substring(message.indexOf(" ") + 1);
                        List<ChatUser> lists = chatLobby.getUser(chatUser);
                        for (ChatUser cu : lists) {
                            if (cu.getNickname().equals(chatUser.getNickname())) {
                                cu.setNickname(name);
                            }
                        }
                    }else {
                        List<ChatUser> chatUsers = chatLobby.getUser(chatUser);
                        for (ChatUser cu : chatUsers) {
                            cu.write(chatUser.getNickname() + " : " + message);
                        }
                    }
                }
            }
        } catch (Exception ex) {
            chatLobby.exit(chatUser);
        }
    }
}

