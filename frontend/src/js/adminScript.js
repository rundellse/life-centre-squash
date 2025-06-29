
const playersUrl = 'http://localhost:8080/api/players';

document.addEventListener('DOMContentLoaded', function() {
    configurePlayerAdd();
    configurePlayerDelete();
    configurePlayerUpdate();
});


function configurePlayerAdd() {
    const submitButton = document.getElementById('new-player-button');
    submitButton.onclick = addPlayer;
}

function addPlayer() {
    const newName = document.getElementById('name-field').value;
    const newEmail = document.getElementById('email-field').value;
    const newPhone = document.getElementById('phone-field').value;
    const newDivision = document.getElementById('division-field').value;
    const newAvailability = document.getElementById('availability-field').value;
    const newAnonymise = document.getElementById('anonymise-field').value;

    if (newName === null || newName === '') {
        alert('Name required for new Player');
        return;
    } else if (newDivision === null || newDivision === '') {
        alert('Division required for new Player');
        return;
    }

    fetch(playersUrl, {
        method: 'POST',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify({
            name: newName,
            email: newEmail,
            phoneNumber: newPhone,
            division: newDivision,
            availabilityNotes: newAvailability,
            anonymise: newAnonymise
        })
    })
    .then(() => {
        console.log('New Player saved');
        location.reload();
    })
    .catch(error => console.error('Error saving new player: ', error));
}


function configurePlayerDelete() {
    const deletePlayersSelect = document.getElementById('delete-player-select');

    fetch(playersUrl, {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(players => {
        players.forEach(player => {
            deletePlayersSelect.innerHTML = deletePlayersSelect.innerHTML + `<option value="${player.id}">${player.name}</option>`;
        });
    })
    .catch(error => console.error('Error fetching players for delete:', error));

    const deleteButton = document.getElementById('delete-player-button');
    deleteButton.onclick = deletePlayer;
}

function deletePlayer() {
    const deletePlayersSelect = document.getElementById('delete-player-select');
    const playerId = deletePlayersSelect.value;
    if (playerId === null || playerId === '') {
        alert('Please select a player for deletion.');
        return;
    }

    if (confirm(`Are you sure you want to delete Player: ${deletePlayersSelect.options[deletePlayersSelect.selectedIndex].text}?`)) {
        fetch(playersUrl + '/' + playerId, {method: 'delete'})
            .then(() => configurePlayerDelete())
            .catch(error => console.error('Error deleting player:', error));
    }
}


function configurePlayerUpdate() {
    const updatePlayerSelect = document.getElementById('update-player-select');

    fetch(playersUrl, {
        method: 'GET',
        credentials: 'include'
    })
    .then(response => response.json())
    .then(players => {
        players.forEach(player => {
            updatePlayerSelect.innerHTML = updatePlayerSelect.innerHTML + `<option value="${player.id}">${player.name}</option>`;
        });
    })
    .catch(error => console.error('Error fetching players for update:', error));

    updatePlayerSelect.onchange = updatePlayerLoad;
    const updateButton = document.getElementById('update-player-button');
    updateButton.onclick = updatePlayer;
}

function updatePlayerLoad() {
    const updateName = document.getElementById('name-update-field');
    const updateEmail = document.getElementById('email-update-field');
    const updatePhone = document.getElementById('phone-update-field');
    const updateDivision = document.getElementById('division-update-field');
    const updateAvailability = document.getElementById('availability-update-field');
    const updateRedFlagged = document.getElementById('red-flagged-update-field');
    const updateAnonymise = document.getElementById('anonymise-update-field');

    const url = 'http://localhost:8080/api/players/' + document.getElementById('update-player-select').value;

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
    const updateName = document.getElementById('name-update-field').value;
    const updateEmail = document.getElementById('email-update-field').value;
    const updatePhone = document.getElementById('phone-update-field').value;
    const updateDivision = document.getElementById('division-update-field').value;
    const updateAvailability = document.getElementById('availability-update-field').value;
    const updateRedFlagged = document.getElementById('red-flagged-update-field').checked;
    const updateAnonymise = document.getElementById('anonymise-update-field').checked;

    const url = 'http://localhost:8080/api/players/' + document.getElementById('update-player-select').value;
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
            anonymise: updateAnonymise
        })
    })
    .then(() => {
        console.log('Player updated');
        location.reload();
    })
    .catch(error => console.error('Error saving player update: ', error));
}
