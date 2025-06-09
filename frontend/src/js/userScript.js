const usersUrl = 'http://localhost:8080/api/user';
var userId;

document.addEventListener('DOMContentLoaded', function() {
    populateUserDetailsFields();
    configureUpdatePlayerButton();
});

function populateUserDetailsFields() {
    const nameField = document.getElementById('name-field');
    const emailField = document.getElementById('email-field');
    const phoneField = document.getElementById('phone-field');
    const availabilityField = document.getElementById('availability-field');
    const anonymiseCheck = document.getElementById('anonymise-check');

    fetch(usersUrl, {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(user => {
        userId = user.id;
        nameField.value = user.name;
        emailField.value = user.email;
        phoneField.value = user.phoneNumber;
        availabilityField.value = user.availabilityNotes;
        anonymiseCheck.checked = user.anonymise;
    })
    .catch(error => console.error('Error loading User details: ', error));
}

function configureUpdatePlayerButton() {
    const updatePlayerButton = document.getElementById("update-player-button");
    updatePlayerButton.onclick = updateUser;
}

function updateUser() {
    const newName = document.getElementById('name-field').value;
    const newEmail = document.getElementById('email-field').value;
    const newPhone = document.getElementById('phone-field').value;
    const newAvailability = document.getElementById('availability-field').value;
    const newAnonymise = document.getElementById('anonymise-check').checked;

    if (newName === null || newName === '') {
        alert('Name is required, please provide.');
        return;
    } else if (newEmail === null || newEmail === '') {
        alert('Email address is required, please provide.');
        return;
    }

    fetch(usersUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify({
            id: userId,
            name: newName,
            email: newEmail,
            phoneNumber: newPhone,
            availabilityNotes: newAvailability,
            anonymise: newAnonymise
        })
    })
    .then(() => {
        console.log('Account details updated');
        // location.reload();
        const updatedP1 = document.getElementById('updated-text');
        updatedP1.innerText = 'Account updated.';
    })
    .catch(error => console.error('Error updating User details: ', error));
}
