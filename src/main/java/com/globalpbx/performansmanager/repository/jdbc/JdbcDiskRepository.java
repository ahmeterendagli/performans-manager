package com.globalpbx.performansmanager.repository.jdbc;

import com.globalpbx.performansmanager.constant.DatabaseConstants;
import com.globalpbx.performansmanager.dto.SystemAlertDto;
import com.globalpbx.performansmanager.dto.SystemPerformansDto;
import com.globalpbx.performansmanager.repository.DiskRepository;
import org.springframework.stereotype.Component;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class JdbcDiskRepository implements DiskRepository {
    @Override
    public void createSystemAlertsTable(Connection connection) {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS " +DatabaseConstants.SYSTEM_ALERTS + " (\n" +
                DatabaseConstants.Id + " INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                DatabaseConstants.LIMIT_OF_DISK_USAGE_PERCENTAGE +" REAL,\n" +
                DatabaseConstants.LIMIT_OF_CPU_USAGE_PERCENTAGE+" REAL,\n" +
                DatabaseConstants.LIMIT_OF_PROCESS_CPU_TIME+ " INTEGER,\n" +
                DatabaseConstants.LIMIT_OF_FREE_SPACE_PERCENTAGE+" REAL,\n" +
                DatabaseConstants.EMAIL_LIST+" TEXT,\n" +
                DatabaseConstants.CREATED_TIME+" TIMESTAMP\n" +
                ");";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public SystemAlertDto saveSystemAlert(Connection connection, SystemAlertDto systemAlertDto) throws SQLException {

        String insertQuery = "INSERT INTO "+ DatabaseConstants.SYSTEM_ALERTS +" (" + DatabaseConstants.LIMIT_OF_DISK_USAGE_PERCENTAGE
                +", " + DatabaseConstants.LIMIT_OF_CPU_USAGE_PERCENTAGE + ", "
                + DatabaseConstants.LIMIT_OF_PROCESS_CPU_TIME + ", " +DatabaseConstants.EMAIL_LIST + ", "
                + DatabaseConstants.LIMIT_OF_FREE_SPACE_PERCENTAGE + ", " + DatabaseConstants.CREATED_TIME + ") \n" +
                "                VALUES (?,?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(insertQuery);

        preparedStatement.setDouble(1, systemAlertDto.getLimitOfDiskUsagePercentage());
        preparedStatement.setDouble(2, systemAlertDto.getLimitOfCpuUsagePercentage());
        preparedStatement.setLong(3, systemAlertDto.getLimitOfProcessCpuTime());
        preparedStatement.setString(4, systemAlertDto.getEmailList().get(0));
        preparedStatement.setDouble(5, systemAlertDto.getLimitOfFreeSpacePercentage());
        preparedStatement.setString(6, String.valueOf(LocalDateTime.now()));

        preparedStatement.executeUpdate();
        return systemAlertDto;
    }

    @Override
    public List<SystemAlertDto> getAllSystemAlert(Connection connection) {
        List<SystemAlertDto> systemAlertDtoList = new ArrayList<>();

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM " + DatabaseConstants.SYSTEM_ALERTS)) {
            while (resultSet.next()) {
                long id = resultSet.getLong("id");
                double limitOfDiskUsagePercentage = resultSet.getDouble(DatabaseConstants.LIMIT_OF_DISK_USAGE_PERCENTAGE);
                double limitOfCpuUsagePercentage = resultSet.getDouble(DatabaseConstants.LIMIT_OF_CPU_USAGE_PERCENTAGE);
                long limitOfProcessCpuTime = resultSet.getLong(DatabaseConstants.LIMIT_OF_PROCESS_CPU_TIME);
                double limitOfFreeSpacePercentage = resultSet.getDouble(DatabaseConstants.LIMIT_OF_FREE_SPACE_PERCENTAGE);
                String emailList = resultSet.getString(DatabaseConstants.EMAIL_LIST);
                LocalDateTime createdTime = LocalDateTime.parse(resultSet.getString(DatabaseConstants.CREATED_TIME));

                SystemAlertDto systemAlertDto = SystemAlertDto.builder()
                        .limitOfDiskUsagePercentage(limitOfDiskUsagePercentage)
                        .limitOfCpuUsagePercentage(limitOfCpuUsagePercentage)
                        .limitOfProcessCpuTime(limitOfProcessCpuTime)
                        .limitOfFreeSpacePercentage(limitOfFreeSpacePercentage)
                        .emailList(Arrays.asList(emailList.split(",")))
                        .build();
                systemAlertDtoList.add(systemAlertDto);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }

        return systemAlertDtoList;
    }
}
