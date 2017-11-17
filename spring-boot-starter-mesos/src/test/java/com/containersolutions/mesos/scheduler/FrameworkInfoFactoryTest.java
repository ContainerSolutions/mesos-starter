package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.mesos.Protos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

/**
 * Tests
 */
@RunWith(MockitoJUnitRunner.class)
public class FrameworkInfoFactoryTest {
    public static final String PRINCIPAL = "principal";
    @Mock
    MesosConfigProperties mesosConfigProperties;
    @Mock
    StateRepository stateRepository;
    @Mock
    CredentialFactory credentialFactory;
    @InjectMocks
    FrameworkInfoFactory factory;

    @Before
    public void before() {
        factory.applicationName = "";
        when(mesosConfigProperties.getRole()).thenReturn("");
        when(stateRepository.getFrameworkID()).thenReturn(Optional.empty());
    }

    @Test
    public void shouldIncludeCredentialsWhenProvided() {
        // When given a valid credential
        when(credentialFactory.create()).thenReturn(getCredential(true));

        // Verify that getPrincipal() contains the principal
        assertEquals(PRINCIPAL, factory.create().getPrincipal());
    }

    @Test
    public void shouldNotIncludeCredentialsWhenNotProvided() {
        // When given a valid credential
        when(credentialFactory.create()).thenReturn(getCredential(false));

        // Verify that getPrincipal() contains the principal
        assertEquals("", factory.create().getPrincipal());
    }

    private Protos.Credential getCredential(Boolean auth) {
        if (auth) {
            return Protos.Credential.newBuilder()
                    .setPrincipal(PRINCIPAL)
                    .setSecret("secret")
                    .build();
        } else {
            return Protos.Credential.getDefaultInstance();
        }
    }
}