
const loginUrl = API_CONFIG.API_BASE_URL + '/login';

document.addEventListener('DOMContentLoaded', function() {
    const loginButton = document.getElementById("login-button");
    loginButton.onclick = submitLogin;
});

function submitLogin() {
    const email = document.getElementById("email-field").value;
    const password = document.getElementById("password-field").value;

    if (email === null || email === '' || password === null || password === '') {
        alert("Please complete login form");
        return;
    }

    fetch(loginUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify({
            email: email,
            password: password
        })
    })
    .then( response => {
        if (response.status == 202) {
            console.log('login successful');
            window.location.href = "index.html";
        }
    })
    .catch(error => console.error('Error during login: ', error));
}
