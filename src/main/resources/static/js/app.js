let isEditMode = false;
let currentTaskId = null;


$(document).ready(function () {
    fetchTasks();

    $("#datepicker").datepicker({
        dateFormat: "yy-mm-dd",
        onSelect: function(dateText) {
            $('#task-header').text(`Задачи на ${dateText}`);
            fetchTasksByDate(dateText.toString())
        }
    });

    $('#search-tasks').on('input', function() {
        let searchTerm = $(this).val().toLowerCase();
        $('#task-list li').each(function() {
            let taskTitle = $(this).find('.task-content span').text().toLowerCase();
            if (taskTitle.includes(searchTerm)) {
                $(this).show();
            } else {
                $(this).hide();
            }
        });
    });


    $("#today-tasks").click(function () {
        $.ajax({
            url: "/api/tasks/today",
            method: "GET",
            success: function (tasks) {
                $('#task-header').text(`Задачи за сегодня`);

                console.log(tasks);
                displayTasks(tasks);
            }
        });
    });

    $("#week-tasks").click(function () {
        $.ajax({
            url: "/api/tasks/week",
            method: "GET",
            success: function (tasks) {
                $('#task-header').text(`Задачи за неделю`);

                console.log(tasks);
                displayTasks(tasks);
            }
        });
    });


    $("#all-tasks").click(function () {
        $('#task-header').text(`Все задачи`);

        fetchTasks()
    });


    $("#show-completed").click(function () {
        $('#task-header').text(`Выполненные задачи`);

        fetchCompleteTasks()
    });


    $("#show-incomplete").click(function () {
        $('#task-header').text(`Невыполненные задачи`);

        fetchIncompleteTasks();
    });


    $("#sort-asc").click(function () {
        $('#task-header').text(`Сортировать по дате ↑`);

        fetchAscTasks();
    });


    $("#sort-desc").click(function () {
        $('#task-header').text(`Сортировать по дате ↓`);

        fetchDescTasks();
    });

    $("#task-modal").dialog({
        autoOpen: false,
        modal: true
    });

    $("#add-task").click(function () {
        openTaskModal(false, null)
    });

    $("#add-task-modal").dialog({
        autoOpen: false,
        modal: true,
        buttons: {},
        close: function() {
            $(this).dialog("close");
        }
    });

    $('#save-task').click(function() {

        let taskTitle = $('#task-title').val();
        let taskDescription = $('#task-description').val();
        let taskCompleted = $('#task-status').is(':checked');

        if (isEditMode && currentTaskId) {
            editTask(currentTaskId, taskTitle, taskDescription, taskCompleted);

        } else {
            addTask(taskTitle, taskDescription);
        }

        closeTaskModal();
    });
});


function fetchTasks() {
    $.ajax({
        url: "/api/tasks",
        method: "GET",
        success: function (data) {

            console.log(data)
            displayTasks(data);
        },
        error: function (error) {
            console.error("Ошибка при получении задач:", error);
        }
    });
}


function fetchTasksByDate(date) {
    $.ajax({
        url: `/api/tasks/date/${date}`,
        method: "GET",
        success: function (data) {
            displayTasks(data);
        }
    });
}

function fetchTasksForToday() {
    $.ajax({
        url: "/api/tasks/today",
        method: "GET",
        success: function (data) {
            displayTasks(data);
        }
    });
}

function fetchCompleteTasks() {
    $.ajax({
        url: "/api/tasks/complete",
        method: "GET",
        success: function (data) {
            displayTasks(data);
        }
    });
}

function fetchIncompleteTasks() {
    $.ajax({
        url: "/api/tasks/incomplete",
        method: "GET",
        success: function (data) {
            displayTasks(data);
        }
    });
}

function fetchAscTasks() {
    $.ajax({
        url: "/api/tasks/asc",
        method: "GET",
        success: function (data) {
            displayTasks(data);
        }
    });
}

function fetchDescTasks() {
    $.ajax({
        url: "/api/tasks/desc",
        method: "GET",
        success: function (data) {
            displayTasks(data);
        }
    });
}


function displayTasks(tasks) {
    $("#task-list").empty();
    tasks.forEach(task => {
        const taskItem = `
            <li data-task-id="${task.id}">
                <div class="task-content">
                    <span>${task.title}</span>
                    <p class="task-description">${task.description.substring(0, 30)}${task.description.length > 30 ? '...' : ''}</p>
                </div>
                
                <div class="task-date">${task.date}</div>
                <button class="delete-task" data-task-id="${task.id}">❌</button>
            </li>`;
        $("#task-list").append(taskItem);
    });

    $("#task-list li").off("click").on("click", function(event) {
        const target = $(event.target);

        if (!target.hasClass("delete-task")) {
            const taskId = $(this).data("task-id");
            openTaskDetails(taskId);
        }
    });

    $(".delete-task").off("click").on("click", function(event) {
        const taskId = $(this).data("task-id");
        deleteTask(taskId);
        event.stopPropagation();
    });
}

function openTaskDetails(taskId) {
    $.ajax({
        url: `/api/tasks/${taskId}`,
        method: "GET",
        success: function(task) {
            openTaskModal(true, task);
        }
    });
}

function openTaskModal(editMode, task) {
    isEditMode = editMode

    $('#task-title').val(editMode && task ? task.title : 'Название');
    $('#task-description').val(editMode && task ? task.description : 'Описание');

    if (isEditMode && task) {
        currentTaskId = task.id;
        $('#task-status-container').show();
        $('#task-status').prop('checked', task.completed);
        $('#task-created-time').show();
        $('#created-time').text(`Создано: ${task.date}`);

    } else {
        currentTaskId = null;
        $('#task-status-container').hide();
        $('#task-created-time').hide();
    }

    $('#task-modal').dialog({
        autoOpen: true,
        modal: true,
        draggable: true,
        resizable: false,
        closeOnEscape: true,
        dialogClass: "no-close-button"
    });

}

function closeTaskModal() {
    $('#task-modal').dialog("close");
}

function closeTaskModal() {
    $('#task-modal').dialog("close");
}

function addTask(taskName, taskDescription) {
    $.ajax({
        url: "/api/tasks/add",
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ title: taskName, description: taskDescription, completed: false }),
        success: function (data) {
            fetchTasks();
        },
        error: function (error) {
            alert("Ошибка при добавлении задачи: " + error.responseText);
        }
    });
}

function editTask(taskId, taskTitle, taskDescription, taskCompleted) {
    $.ajax({
        url: `/api/tasks/edit/${taskId}`,
        method: "POST",
        contentType: "application/json",
        data: JSON.stringify({ title: taskTitle, description: taskDescription, completed: taskCompleted, id: taskId}),
        success: function (data) {
            fetchTasks(); // Обновить список задач после добавления
        },
        error: function (error) {
            alert("Ошибка при добавлении задачи: " + error.responseText);
        }
    });
}

function deleteTask(taskId) {
    $.ajax({
        url: `/api/tasks/delete/${taskId}`,
        method: "POST",
        success: function(data) {
            fetchTasks();
        },
        error: function(error) {
            alert("Ошибка при удалении задачи: " + error.responseText);
        }
    });
}

