'use strict';

let stompClient = null;
let currentSubscription;
let username = document.cookie.split(",")[0];
let topic = document.cookie.split(",")[1];
let socket = new SockJS('/ws');

async function uploadImage() {

    let file = document.querySelector('input[type=file]')['files'][0];

    let reader = new FileReader();
    console.log("next");

    reader.onload = function () {
        if (reader.result.length > 199999) {
            alert("File is too big!");
            return;
        }

        if (reader.result.search()) {

        }

        let base64String = reader.result.replace("data:", "")
            .replace(/^.+,/, "");

        let chatMessage = {
            sender: username,
            content: base64String,
            type: 'PICTURE'
        };
        stompClient.send(`${topic}/sendMessage`, {}, JSON.stringify(chatMessage));
    }

    try {
        reader.readAsDataURL(file);
    } catch (e) {
        console.log(e);
    }


}

//for registration notification
let registerSocket = new SockJS('/ws');
let registerStompClient = null;
let currentRegistrationSubscription;

// Enter new room and leave the other one
function enterRoom(newRoomId) {
    let roomId = newRoomId;
    //setting roomId cookie
    document.cookie = username + "," + roomId;
    console.log(roomId)
    //if roomId is not public, it will be in form of user1user2
    //with user1 or user2 being our username, so we remove the username
    if (roomId !== "public") {
        var toUser = roomId.replace(username, "");
    } else {
        toUser = roomId;
    }

    document.getElementById('selected_user_name').innerHTML = toUser;
    topic = `/app/chat/${newRoomId}`;

    if (currentSubscription) {
        currentSubscription.unsubscribe();
    }
    currentSubscription = stompClient.subscribe(`/channel/${roomId}`, onMessageReceived);


    stompClient.send(`${topic}/addUser`,
        {},
        JSON.stringify({sender: username, type: 'JOIN', destination: roomId})
    );
}

// Connect to WebSocket Server on first time initialising
async function onConnected() {
    enterRoom(topic);
}

// Send message to the server, chatrooms
async function sendMessage(event) {
    // check if event.key is literal string
    if (event.key === '"') {
        event.preventDefault();
        return;
    }
    let messageInput = document.getElementById("chat_box").value;

    // check if enter key was pressed
    if (event.keyCode !== 13) {
        return;
    }


    if (messageInput === "") {
        event.preventDefault();
        return;
    }

    //let messageContent = messageInput.value.trim();
    let chatMessage = {
        sender: username,
        content: messageInput,
        type: 'CHAT'
    };
    stompClient.send(`${topic}/sendMessage`, {}, JSON.stringify(chatMessage));
    document.getElementById("chat_box").value = "";

    event.preventDefault();
}

// Receive message from the server either JOIN(when a user enters the room) or CHAT(when a user sends message) or LEAVE(when user leaves the room)
async function onMessageReceived(payload) {
    let message = JSON.parse(payload.body);

    if (message.type === 'CHAT') {
        if (message.sender === username) {
            document.getElementById("chat_message_box").innerHTML += ` <li class="chat-left">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + message.sender + `</div>
                                                    </div>
                                                    <div class="chat-text">` + message.content + `
                                                    </div>
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                </li>`


        } else {
            document.getElementById("chat_message_box").innerHTML += `<li class="chat-right">
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                    <div class="chat-text">` + message.content + `
                                                    </div>
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + message.sender + `</div>
                                                    </div>
                                                </li>`


        }

        getActiveUsers();
        document.getElementById("scroller").scrollBy(0, 100000);
    } else if (message.type === 'JOIN' && message.sender !== username) {
        document.getElementById(message.sender).innerHTML = `
                                                                      <div class="user">
                                                                         <img src="https://www.bootdey.com/img/Content/avatar/avatar4.png">
                                                                             <span class="status online"></span>
                                                                      </div>
                                                                        <p class="name-time"><span class="name">` + message.sender + `</span></p>
                                                                      `
        getActiveUsers();

    } else if (message.type === 'PICTURE') {
        if (message.sender === username) {
            document.getElementById("chat_message_box").innerHTML += ` <li class="chat-left">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + message.sender + `</div>
                                                    </div>
                                                    <img src="data:image/jpeg;base64,` + message.content + `">
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                </li>`
            document.getElementById("scroller").scrollBy(0, 100000);

        } else {
            document.getElementById("chat_message_box").innerHTML += `<li class="chat-right">
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                    <img src="data:image/jpeg;base64,` + message.content + `">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + message.sender + `</div>
                                                    </div>
                                                </li>`

        }
        getActiveUsers();
    } else if (message.type === 'RECORDING') {
        //convert base64 to audio ogg blob
        //get a url of the blob and set it to the audio tag
        //play the audio
        let binary = convertURIToBinary(message.content);
        let blob = new Blob([binary], {
            type: 'audio/ogg'
        });
        let blobUrl = URL.createObjectURL(blob);


        if (message.sender === username) {
            document.getElementById("chat_message_box").innerHTML += ` <li class="chat-left">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + message.sender + `</div>
                                                    </div>
                                                    <audio controls type="audio/ogg" src="` + blobUrl + `"></audio>
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                </li>`
            document.getElementById("scroller").scrollBy(0, 1000000);

        } else {
            document.getElementById("chat_message_box").innerHTML += `<li class="chat-right">
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                    <audio controls type="audio/ogg" src="` + blobUrl + `"></audio>
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + message.sender + `</div>
                                                    </div>
                                                </li>`

        }
        document.getElementById("scroller").scrollBy(0, 1000000);

    } else if (message.type === 'LEAVE' && message.sender !== username) {
        if (document.getElementById(message.sender) !== null) {
            /*document.getElementById(message.sender).remove();*/
            document.getElementById(message.sender).innerHTML = `
                                                                      <div class="user">
                                                                         <img src="https://www.bootdey.com/img/Content/avatar/avatar4.png">
                                                                             <span class="status offline"></span>
                                                                      </div>
                                                                        <p class="name-time"><span class="name">` + message.sender + `</span></p>
                                                                      `

        }
        getActiveUsers();
    }
}

// Load message history from the server and enters a room
 function changeChatRoom(roomId) {
    enterRoom(roomId);
    loadMessageHistory(roomId);
    document.getElementById("scroller").scrollBy(0, 1000000);
}

//helper function to load message history from the server
async function loadMessageHistory(room_id_name) {

    let settings = {
        "url": "http://localhost:8080/download/chat/" + room_id_name + "/messages",
        "method": "GET"
    };

    $.ajax(settings).done(function (response) {

        document.getElementById("chat_message_box").innerHTML = "";
        let chatHistory = response;

        for (let i = 0; i < chatHistory.length; i++) {
            if (chatHistory[i].type === 'CHAT') {
                console.log(chatHistory[i].sender + " TYPE:" + chatHistory[i].type);
                if (chatHistory[i].sender === username) {

                    // use <br> to cut the text into two lines
                    document.getElementById("chat_message_box").innerHTML += ` <li class="chat-left">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + chatHistory[i].sender + `</div>
                                                    </div>
                                                    <div class="chat-text">` + chatHistory[i].content + `
                                                    </div>
                                                    <div class="chat-hour">` + chatHistory[i].time.slice(11, 16) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                </li>`

                } else {
                    document.getElementById("chat_message_box").innerHTML += `<li class="chat-right">
                                                    <div class="chat-hour">` + chatHistory[i].time.slice(11, 16) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                    <div class="chat-text">` + chatHistory[i].content + `
                                                    </div>
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + chatHistory[i].sender + `</div>
                                                    </div>
                                                </li>`


                }
            } else if (chatHistory[i].type === 'PICTURE') {
                console.log(chatHistory[i].sender + " TYPE:" + chatHistory[i].type);
                if (chatHistory[i].sender === username) {

                    // use <br> to cut the text into two lines
                    document.getElementById("chat_message_box").innerHTML += ` <li class="chat-left">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + chatHistory[i].sender + `</div>
                                                    </div>
                                                    <img src="data:image/jpeg;base64,` + chatHistory[i].content + `">
                                                    <div class="chat-hour">` + chatHistory[i].time.slice(11, 16) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                </li>`

                } else {
                    document.getElementById("chat_message_box").innerHTML += `<li class="chat-right">
                                                    <div class="chat-hour">` + chatHistory[i].time.slice(11, 16) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                    <img src="data:image/jpeg;base64,` + chatHistory[i].content + `">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + chatHistory[i].sender + `</div>
                                                    </div>
                                                </li>`


                }
            } else if (chatHistory[i].type === 'RECORDING') {
                //convert base64 to audio ogg blob
                //get a url of the blob and set it to the audio tag
                //play the audio
                let binary = convertURIToBinary(chatHistory[i].content);
                let blob = new Blob([binary], {
                    type: 'audio/ogg'
                });
                let blobUrl = URL.createObjectURL(blob);


                if (chatHistory[i].sender === username) {
                    document.getElementById("chat_message_box").innerHTML += ` <li class="chat-left">
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + chatHistory[i].sender + `</div>
                                                    </div>
                                                    <audio controls type="audio/ogg" src="` + blobUrl + `"></audio>
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                </li>`
                    document.getElementById("scroller").scrollBy(0, 100000);

                } else {
                    document.getElementById("chat_message_box").innerHTML += `<li class="chat-right">
                                                    <div class="chat-hour">` + new Date().toLocaleTimeString().slice(0, -3) + `<span
                                                            class="fa fa-check-circle"></span></div>
                                                    <audio controls type="audio/ogg" src="` + blobUrl + `"></audio>
                                                    <div class="chat-avatar">
                                                        <img src="https://www.bootdey.com/img/Content/avatar/avatar3.png">
                                                        <div class="chat-name">` + chatHistory[i].sender + `</div>
                                                    </div>
                                                </li>`

                }
            }
        }


    });
    document.getElementById("scroller").scrollBy(0, 100000);
}



//When page is ready
$(document).ready(function () {
    //setting up the register websocket
    registerStompClient = Stomp.over(registerSocket)
    registerStompClient.connect({}, onRegisterSocketConnected);

    //setting up the actual livechat websocket
    stompClient = Stomp.over(socket);
    stompClient.connect({}, onConnected);
    //setting up the chat room list
    document.getElementById('selected_user_name').innerHTML = topic;
    //setting up username greeter
    document.getElementById('username_greeter').value = username;

    //setting up recording buttons
    document.getElementById("stop").disabled = true;
    document.getElementById("start").disabled = true;
    //getting all registered users
    getAllRegisteredUsers();
    //setting up the active users list
    getActiveUsers();
    //loading the message history of the current topic(room)
    loadMessageHistory(topic);
    //scrolling to the bottom of the chat box
    document.getElementById("scroller").scrollBy(0, 100000);
});

async function getActiveUsers() {
    let settings = {
        "url": "http://localhost:8080/download/chat/users",
        "method": "GET",
    };

    $.ajax(settings).done(function (response) {
        console.log("USERS:" + response);
        if (response.length > 0) {

            for (let i = 0; i < response.length; i++) {
                if (response[i] !== username) {
                    if (document.getElementById(response[i]) === null) {
                        window.location.reload();
                        return;
                    }
                    document.getElementById(response[i]).innerHTML = `
                                                                      <div class="user">
                                                                         <img src="https://www.bootdey.com/img/Content/avatar/avatar4.png">
                                                                             <span class="status online"></span>
                                                                      </div>
                                                                        <p class="name-time"><span class="name">` + response[i] + `</span></p>
                                                                      `
                }
            }
        } else {
            console.log("No online users found yet!");
        }
    });
}

// Logouts the user from the chatroom and clears the session/cookies
function logout() {
    stompClient.send(`${topic}/leave`,
        {},
        JSON.stringify({sender: username, type: 'LEAVE'})
    );
    let settings = {
        "url": "http://localhost:8080/logout",
        "method": "POST",
        "timeout": 0,
    };

    $.ajax(settings).done(function () {
        window.location.href = "http://localhost:8080/";
    });


}

async function getAllRegisteredUsers() {
    let settings = {
        "url": "http://localhost:8080/download/chat/registeredUsers",
        "method": "GET",
    };

    $.ajax(settings).done(function (response) {
        for (let i = 0; i < response.length; i++) {
            if (response[i] !== username) {
                var socketByAlphabeticalOrder;
                if (username.localeCompare(response[i]) === -1) {
                    socketByAlphabeticalOrder = username + response[i];
                } else {
                    socketByAlphabeticalOrder = response[i] + username;
                }
                console.log(socketByAlphabeticalOrder);
                document.getElementById("active_users").innerHTML += `<li class="person" onclick="changeChatRoom(\`` + socketByAlphabeticalOrder + `\`)" data-chat="person4" id="` + response[i] + `">
                                                                            <div class="user">
                                                                                <img src="https://www.bootdey.com/img/Content/avatar/avatar4.png">
                                                                                    <span class="status offline"></span>
                                                                            </div>
                                                                                <p class="name-time"><span class="name">` + response[i] + `</span>
                                                                                </p>
                                                                        </li>`
            }

        }
    });
}

// Setups the register callback socket connection
async function onRegisterSocketConnected() {

    if (currentRegistrationSubscription) {
        currentRegistrationSubscription.unsubscribe();
    }
    currentRegistrationSubscription = stompClient.subscribe(`/channel/registerCallbackSocket`, registerMessageReceived);
}

//wrapper function to parse the registered user's name and add it to the list
async function registerMessageReceived(payload) {
    let message = JSON.parse(payload.body);
    if (message.type === 'REGISTER') {
        var socketByAlphabeticalOrder;
        if (username.localeCompare(message.content) === -1) {
            socketByAlphabeticalOrder = username + message.content;
        } else {
            socketByAlphabeticalOrder = message.content + username;
        }
        console.log(socketByAlphabeticalOrder);
        document.getElementById("active_users").innerHTML += `<li class="person"  onclick="changeChatRoom(\`` + socketByAlphabeticalOrder + `\`)" data-chat="person4" id="` + message.content + `">
                                                                            <div class="user">
                                                                                <img src="https://www.bootdey.com/img/Content/avatar/avatar4.png">
                                                                                    <span class="status offline"></span>
                                                                            </div>
                                                                                <p class="name-time"><span class="name">` + message.content + `</span>
                                                                                </p>
                                                                        </li>`
    }else if(message.type === 'REMOVE'){
        document.getElementById(message.content).remove();
    }
    console.log(message);
}


//Account settings button pressed
async function settingsPage() {
    //Notify the server that the user is leaving the chatroom
    let chatMessage = {
        sender: username,
        content: null,
        type: 'LEAVE'
    };
    stompClient.send(`${topic}/sendMessage`, {}, JSON.stringify(chatMessage));

    window.location.href = "http://localhost:8080/settings";
}


///////AUDIO FUNCTIONS////////
let recorder;
let chunks;

function getPermission() {
    navigator.mediaDevices.getUserMedia({audio: true}).then(_stream => {
        document.getElementById("start").disabled = false;
        document.getElementById("micPermission").disabled = true;
        recorder = new MediaRecorder(_stream);
        recorder.ondataavailable = e => {
            chunks.push(e.data);
            if (recorder.state === 'inactive') makeLink();
        };
    });
}

function record() {
    document.getElementById("stop").disabled = false;
    document.getElementById("start").disabled = true;
    chunks = [];

    try {
        recorder.start();
    } catch (e) {
        console.log("Give permission first!");
    }

}

function stopRecord() {
    document.getElementById("stop").disabled = true;
    document.getElementById("start").disabled = false;
    try {
        recorder.stop();
    } catch (e) {
        console.log("Recording stopped or has not started yet!");
    }
}

function makeLink() {
    let blob = new Blob(chunks, {type: 'audio/ogg'});
    let reader = new FileReader();

    /////////////////////FUTURE ME, DONT CHANGE THIS!!!!

    reader.onloadend = function () {
        var base64data = reader.result;
        //console.log(base64data);

        //send the audio to socket...
        console.log("Sending audio to socket...");
        let chatMessage = {
            sender: username,
            content: base64data,
            type: 'RECORDING'
        };
        stompClient.send(`${topic}/sendMessage`, {}, JSON.stringify(chatMessage));
    }
    reader.readAsDataURL(blob);
    ////////////////////////


    //converting from arrayBuffer to blob in order to display to browser(server(socket) -> client)
    //let new_blob = new Blob([new Uint8Array(arrayBuffer)]);


    //Convert blob to url to display it in a player with controls
    //let url = URL.createObjectURL(blob);
    //document.getElementById("scroller").innerHTML = `<audio controls="" src="` + url + `"></audio>`
}

//from base64 to ogg audio blob file
function convertURIToBinary(dataURI) {
    let BASE64_MARKER = ';base64,';
    let base64Index = dataURI.indexOf(BASE64_MARKER) + BASE64_MARKER.length;
    let base64 = dataURI.substring(base64Index);
    let raw = window.atob(base64);
    let rawLength = raw.length;
    let arr = new Uint8Array(new ArrayBuffer(rawLength));

    for (let i = 0; i < rawLength; i++) {
        arr[i] = raw.charCodeAt(i);
    }
    return arr;
}