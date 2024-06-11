package com.milkwind.infrastructurestation.arknightsinfrastructurestationbackend;

import com.arknightsinfrastructurestationbackend.mapper.user.UploadStagingWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.mapper.user.UploadWorkFileCountMapper;
import com.arknightsinfrastructurestationbackend.service.timedTasks.ScheduledTasksService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ScheduledTasksServiceTest {

    @Mock
    private UploadWorkFileCountMapper uploadWorkFileCountMapper;

    @Mock
    private UploadStagingWorkFileCountMapper uploadStagingWorkFileCountMapper;

    @InjectMocks
    private ScheduledTasksService scheduledTasksService;

    @Before
    public void setUp() {
        // This setup is now simplified as @InjectMocks takes care of instantiation.
    }

    @Test
    public void testResetUploadCount() {
        // Act
        scheduledTasksService.resetUploadCount();

        // Assert
        // Verify that delete method is called once for each mapper
        verify(uploadWorkFileCountMapper, times(1)).truncateTable();
        verify(uploadStagingWorkFileCountMapper, times(1)).truncateTable();
    }
}
