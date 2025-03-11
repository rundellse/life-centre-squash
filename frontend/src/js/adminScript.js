
const apiUrl = 'http://localhost:8080/api/players';

document.addEventListener('DOMContentLoaded', function() {
    configurePlayerAdd();
    configurePlayerDelete();
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

    if (newName === null || newName === '') {
        alert('Name required for new Player');
        return;
    }
    if (newDivision === null || newDivision === '') {
        alert('Division required for new Player');
        return;
    }

    fetch(apiUrl, {
        method: 'post',
        headers: {
            'Content-type': 'application/json; charset=UTF-8'
        },
        body: JSON.stringify({
            name: newName,
            email: newEmail,
            phoneNumber: newPhone,
            division: newDivision,
            availabilityNotes: newAvailability
        })
    })
        .then(response => {
            console.log('New Player saved');
            location.reload();
        })
        .catch(error => console.error('Error saving new player: ', error));
}

function configurePlayerDelete() {
    const deletePlayersSelect = document.getElementById('delete-player-select');
    deletePlayersSelect.innerHTML = '';

    fetch(apiUrl)
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
        fetch(apiUrl + '/' + playerId, {method: 'delete'})
            .then(() => configurePlayerDelete())
            .catch(error => console.error('Error deleting player:', error));
    }
}
