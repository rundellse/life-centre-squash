const logoutUrl = API_CONFIG.API_BASE_URL + '/logout';

document.addEventListener('DOMContentLoaded', function() {
    await configureLogoutButton();
});

async function configureLogoutButton() {
    const logoutButton = document.getElementById('logout-button');
    logoutButton.onclick = logout;
}

function logout() {
    fetch(logoutUrl, {
        method: 'POST',
        credentials: 'include'
    })
    .then(response => {
        if (response.status == 200) {
            console.log('Logout completed successfully.');
            window.location = 'login.html';
        } else {
            throw new Error('Logout not completed. Non-OK response code returned: ' + response.status);
        }
    })
    .catch(error => console.error('Error while logging out: ', error));
}
