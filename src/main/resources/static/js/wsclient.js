let socket = new WebSocket(location.protocol !== 'https:' ?
    `ws://${window.location.hostname}:8080/console` :
    `wss://${window.location.hostname}/apps/webterminal/console`);

const commandInput = document.getElementById('input-command');
const host = document.getElementById('host');
const port = document.getElementById('port');
const username = document.getElementById('username');
const password = document.getElementById('password');
const terminal = document.getElementById('terminal');
const span = document.getElementById('user-host');
const closeButton = document.getElementById('close-session');
const clear = document.getElementById('clear');

let request = {};


document.forms.connection.onsubmit = function () {
    request.sessionUser = '';
    request.host = host.value;
    request.port = port.value;
    request.username = username.value;
    request.password = password.value;
    request.command = 'echo ${USER}@${HOSTNAME}';
    socket.send(JSON.stringify(request));
    return false;
};
socket.addEventListener('open', function (event) {
    terminal.value += '🤝 Connection opened, enter session details and click Open session. \r\n';
});


commandInput.addEventListener('keypress', function (event) {

        if (event.key === "Enter") {
            event.preventDefault();
            request.command = this.value;
            socket.send(JSON.stringify(request));
            this.value = '';

        }
    }
)


socket.addEventListener('message', function (event) {

    if (event.data.startsWith(`${username.value}@`)) {
        span.textContent = event.data + ' $';
        request.sessionUser = event.data;
        terminal.value += 'Session opened -> ' + event.data + '\r\n';
        commandInput.focus();
        return;
    }

    terminal.value += event.data + '\r\n';
    terminal.scrollTop = terminal.scrollHeight;

});

clear.addEventListener('click', function (event) {
    event.preventDefault();
    terminal.value = '';
})
closeButton.addEventListener('click', function (event) {
    event.preventDefault();
    socket.close();
})

socket.addEventListener('close', function (event) {
    terminal.value += '⚡ Connection closed, reload page to resume session. \r\n';
    host.value = '';
    port.value = '';
    username.value = '';
    password.value = '';
});