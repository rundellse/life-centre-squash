const passwordUrl = API_CONFIG.API_BASE_URL + '/user/password';
var userId;

document.addEventListener('DOMContentLoaded', function() {
    configureUpdatePasswordButton();
});

function configureUpdatePasswordButton() {
    const updatePasswordButton = document.getElementById('update-password-button');
    updatePasswordButton.onclick = updateUser;
}

function updateUser() {
    const currentPassword = document.getElementById('old-password-field').value;
    const newPassword = document.getElementById('new-password-field').value;
    const verifyPassword = document.getElementById('verify-password-field').value;

    const warningText = document.getElementById('warning-text');

    if (currentPassword === null || currentPassword === '') {
        warningText.innerText = 'Current password is required.';
        return;
    } else if (newPassword === null || newPassword === '') {
        warningText.innerText = 'New password is required.';
        return;
    } else if (verifyPassword === null || verifyPassword === '') {
        warningText.innerText = 'Verify new password is required.';
        return;
    } else if (newPassword !== verifyPassword) {
        warningText.innerText = 'Passwords do not match.';
        return;
    }

    fetch(passwordUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify({
            currentPassword: currentPassword,
            newPassword: newPassword
        })
    })
    .then(() => {
        console.log('Password updated');
        // location.reload();
        const updatedP1 = document.getElementById('updated-text');
        updatedP1.innerText = 'Password Updated';
    })
    .catch(error => console.error('Error updating password: ', error));
}
