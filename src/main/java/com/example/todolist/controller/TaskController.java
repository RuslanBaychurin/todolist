package com.example.todolist.controller;

import com.example.todolist.db.DatabaseConnection;
import com.example.todolist.model.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    // Получение всех задач
    @GetMapping
    public List<Task> getAllTasks() {
        List<Task> tasks = new ArrayList<>();
        String selectSQL = "SELECT * FROM tasks";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(selectSQL)) {

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDate(rs.getString("date"));
                task.setDescription(rs.getString("description"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return tasks;
    }

    // Получение задачи по ID
    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        String selectSQL = "SELECT * FROM tasks WHERE id = ?";
        Task task = new Task();


        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setLong(1, id);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));

                task.setCompleted(rs.getBoolean("completed"));

                return ResponseEntity.ok(task);
            } else {
                return ResponseEntity.status(404).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Добавление новой задачи
    @PostMapping("/add")
    public ResponseEntity<?> addTask(@RequestBody Task task) {
        String insertSQL = "INSERT INTO tasks (title, description, date, completed) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {

            Date sqlDate = new Date(System.currentTimeMillis());

            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setString(3, sqlDate.toString());
            pstmt.setBoolean(4, task.isCompleted());
            pstmt.executeUpdate();

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Редактирование задачи
    @PostMapping("/edit/{id}")
    public ResponseEntity<?> editTask(@RequestBody Task task) {
        String updateSQL = "UPDATE tasks SET title = ?, description = ?, completed = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(updateSQL)) {

            pstmt.setString(1, task.getTitle());
            pstmt.setString(2, task.getDescription());
            pstmt.setBoolean(3, task.isCompleted());
            pstmt.setLong(4, task.getId());
            pstmt.executeUpdate();

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // Удаление задачи
    @PostMapping("/delete/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        String deleteSQL = "DELETE FROM tasks WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(deleteSQL)) {

            pstmt.setLong(1, id);
            int rowsAffected = pstmt.executeUpdate();

            if (rowsAffected > 0) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(404).build();
            }
        } catch (SQLException e) {
            return ResponseEntity.status(500).build();
        }
    }

    //Задачи на сегодня
    @GetMapping("/today")
    public List<Task> getTasksForToday() {
        List<Task> tasks = new ArrayList<>();

        String selectSQL = "SELECT * FROM tasks WHERE date = ?";
        Date sqlDate = new Date(System.currentTimeMillis());

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setString(1, sqlDate.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    //Задачи на эту неделю
    @GetMapping("/week")
    public List<Task> getTasksForWeek() {
        List<Task> tasks = new ArrayList<>();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();

        String currentDate = sdf.format(calendar.getTime());

        calendar.add(Calendar.DAY_OF_MONTH, -7);
        String pastDate = sdf.format(calendar.getTime());

        String selectSQL = "SELECT * FROM tasks WHERE date BETWEEN ? AND ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setString(1, pastDate);
            pstmt.setString(2, currentDate);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    //Выполненные задачи
    @GetMapping("/complete")
    public List<Task> getCompleteTasks() {
        List<Task> tasks = new ArrayList<>();

        String selectSQL = "SELECT * FROM tasks WHERE completed = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setBoolean(1, true);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    //Невыполненные задачи
    @GetMapping("/incomplete")
    public List<Task> getInCompleteTasks() {
        List<Task> tasks = new ArrayList<>();

        String selectSQL = "SELECT * FROM tasks WHERE completed = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setBoolean(1, false);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    //Сортировать по дате ↑
    @GetMapping("/asc")
    public List<Task> getTasksAsc() {
        List<Task> tasks = new ArrayList<>();

        String selectSQL = "SELECT * FROM tasks ORDER BY date ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }

    //Сортировать по дате ↓
    @GetMapping("/desc")
    public List<Task> getTasksDesc() {
        List<Task> tasks = new ArrayList<>();

        String selectSQL = "SELECT * FROM tasks ORDER BY date DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }


    //Задачи на определенную дату
    @GetMapping("/date/{date}")
    public List<Task> getTasksByDate(@PathVariable String date) {
        List<Task> tasks = new ArrayList<>();

        System.out.println("date: " + date);

        String selectSQL = "SELECT * FROM tasks WHERE date = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {

            pstmt.setString(1, date);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                Task task = new Task();
                task.setId(rs.getLong("id"));
                task.setTitle(rs.getString("title"));
                task.setDescription(rs.getString("description"));
                task.setDate(rs.getString("date"));
                task.setCompleted(rs.getBoolean("completed"));
                tasks.add(task);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return tasks;
    }
}
