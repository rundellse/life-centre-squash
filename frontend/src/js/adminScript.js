
const playersUrl = API_CONFIG.API_BASE_URL + '/players';

const selectTopLineOption = '<option disabled selected value> -- Select a Player -- </option>';


document.addEventListener('DOMContentLoaded', async function() {
    await configurePlayerAdd();
    await configurePlayerDelete();
    await configurePlayerUpdate();
});


async function configurePlayerAdd() {
    const submitButton = document.getElementById('new-player-button');
    submitButton.onclick = addPlayer;
}

function addPlayer() {
    const nameField = document.getElementById('name-field');
    const newName = nameField.value;
    const emailField = document.getElementById('email-field');
    const newEmail = emailField.value;
    const phoneField = document.getElementById('phone-field');
    const newPhone = phoneField.value;
    const divisionField = document.getElementById('division-field');
    const newDivision = divisionField.value;
    const availabilityField = document.getElementById('availability-field');
    const newAvailability = availabilityField.value;
    const anonymiseField = document.getElementById('anonymise-field');
    const newAnonymise = anonymiseField.checked;
    const adminField = document.getElementById('admin-field');
    const newAdminUser = adminField.checked;
    const passwordField = document.getElementById('password-field');
    const newPassword = String(passwordField.value);

    if (newName === null || newName === '') {
        alert('Name required for new Player');
        return;
    } else if (newDivision === null || newDivision === '') {
        alert('Division required for new Player');
        return;
    } else if (newPassword === null || newPassword === '' || newPassword.length < 8) {
        alert('Password required with 8 or more characters')
        return;
    }

    fetch(playersUrl, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify({
            name: newName,
            email: newEmail,
            phoneNumber: newPhone,
            division: newDivision,
            availabilityNotes: newAvailability,
            anonymise: newAnonymise,
            adminUser: newAdminUser,
            password: newPassword
        })
    })
    .then(() => {
        console.log('New Player saved');
        nameField.value = '';
        emailField.value = '';
        phoneField.value = '';
        divisionField.value = '';
        availabilityField.value = '';
        anonymiseField.value = '';
        adminField.value = '';

        passwordField.value = '';
        loadPlayerDeleteSelect();
        const updatePlayerSelect = document.getElementById('update-player-select');
        loadPlayerUpdateSelect(updatePlayerSelect);
    })
    .catch(error => console.error('Error saving new player: ', error));
}


async function configurePlayerDelete() {
    await loadPlayerDeleteSelect();

    const deleteButton = document.getElementById('delete-player-button');
    deleteButton.onclick = deletePlayer;
}

async function loadPlayerDeleteSelect() {
    const deletePlayersSelect = document.getElementById('delete-player-select');

    const response = await fetch(playersUrl, {
        method: 'GET',
        credentials: 'include'
    });
    const players = await response.json();

    deletePlayersSelect.innerHTML = selectTopLineOption;
    players.forEach(player => {
        deletePlayersSelect.innerHTML = deletePlayersSelect.innerHTML + `<option value="${player.id}">${player.name}</option>`;
    });
}

function deletePlayer() {
    const deletePlayersSelect = document.getElementById('delete-player-select');
    const playerId = deletePlayersSelect.value;
    if (playerId === null || playerId === '') {
        alert('Please select a player for deletion.');
        return;
    }

    if (confirm(`Are you sure you want to delete Player: ${deletePlayersSelect.options[deletePlayersSelect.selectedIndex].text}?`)) {
        fetch(playersUrl + '/' + playerId, {
            method: 'delete',
            credentials: 'include'
        })
        .then(() => configurePlayerDelete())
        .catch(error => console.error('Error deleting player:', error));
    }
}


async function configurePlayerUpdate() {
    const updatePlayerSelect = document.getElementById('update-player-select');
    await loadPlayerUpdateSelect(updatePlayerSelect);

    updatePlayerSelect.onchange = updatePlayerLoad;
    const updateButton = document.getElementById('update-player-button');
    updateButton.onclick = updatePlayer;
}

async function loadPlayerUpdateSelect(updatePlayerSelect) {
    const response = await fetch(playersUrl, {
        method: 'GET',
        credentials: 'include'
    });
    const players = await response.json();

    updatePlayerSelect.innerHTML = selectTopLineOption;
    players.forEach(player => {
        updatePlayerSelect.innerHTML = updatePlayerSelect.innerHTML + `<option value="${player.id}">${player.name}</option>`;
    });
}


function configurePasswordUpdate() {
    const updatePlayerPasswordButton = document.getElementById('password-update-button');
    updatePlayerPasswordButton.onclick = updatePlayerPasswordButton;
}

function updatePlayerLoad() {
    const updateName = document.getElementById('name-update-field');
    const updateEmail = document.getElementById('email-update-field');
    const updatePhone = document.getElementById('phone-update-field');
    const updateDivision = document.getElementById('division-update-field');
    const updateAvailability = document.getElementById('availability-update-field');
    const updateRedFlagged = document.getElementById('red-flagged-update-field');
    const updateAnonymise = document.getElementById('anonymise-update-field');
    const updateAdminUser = document.getElementById('admin-update-field');

    const url = API_CONFIG.API_BASE_URL + '/players/' + document.getElementById('update-player-select').value;

    fetch(url, {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(player => {
        updateName.value = player.name;
        updateEmail.value = player.email;
        updatePhone.value = player.phoneNumber;
        updateDivision.value = player.division;
        updateAvailability.innerHTML = player.availabilityNotes;
        updateRedFlagged.checked = player.redFlagged;
        updateAnonymise.checked = player.anonymise;
    })
    .catch(error => console.error('Error while fetching player details:', error));
}

function updatePlayer() {
    const updateNameField = document.getElementById('name-update-field');
    const updateName = updateNameField.value;
    const updateEmailField = document.getElementById('email-update-field');
    const updateEmail = updateEmailField.value;
    const updatePhoneField = document.getElementById('phone-update-field');
    const updatePhone = updatePhoneField.value;
    const updateDivisionField = document.getElementById('division-update-field');
    const updateDivision = updateDivisionField.value;
    const updateAvailabilityField = document.getElementById('availability-update-field');
    const updateAvailability = updateAvailabilityField.value;
    const updateRedFlaggedField = document.getElementById('red-flagged-update-field');
    const updateRedFlagged = updateRedFlaggedField.checked;
    const updateAnonymiseField = document.getElementById('anonymise-update-field');
    const updateAnonymise = updateAnonymiseField.checked;
    const updateAdminUserField = document.getElementById('admin-update-field');
    const updateAdminUser = updateAdminUserField.checked;

    const url = API_CONFIG.API_BASE_URL + '/players/' + document.getElementById('update-player-select').value;
    fetch(url, {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify({
            name: updateName,
            email: updateEmail,
            phoneNumber: updatePhone,
            division: updateDivision,
            availabilityNotes: updateAvailability,
            redFlagged: updateRedFlagged,
            anonymise: updateAnonymise,
            adminUser: updateAdminUser
        })
    })
    .then(() => {
        console.log('Player details successfully updated');
        updateNameField.value = '';
        updateEmailField.value = '';
        updatePhoneField.value = '';
        updateDivisionField.value = '';
        updateAvailabilityField.value = '';
        updateRedFlaggedField.value = '';
        updateAnonymiseField.value = '';
        updateAdminUserField.value = '';
    })
    .catch(error => console.error('Error saving player update: ', error));
}

function updatePlayerPassword() {
    const passwordInput = document.getElementById('password-update-field');
    const updatePlayer = document.getElementById('update-player-select').value;
    const updatePassword = passwordInput.value;

    const url = API_CONFIG.API_BASE_URL + '/admin/password';
    fetch(url, {
        method: 'POST',
        credentials: 'include',
        body: JSON.stringify({
            playerId: updatePlayer,
            newPassword: updatePassword
        })
    })
    .then(() => {
        console.log('Player password successfully updated.');
        passwordInput.value = '';
    })
    .catch(error => console.error('Error saving player update: ', error));
}
