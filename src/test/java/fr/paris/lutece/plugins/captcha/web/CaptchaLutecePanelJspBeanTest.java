/*
 * Copyright (c) 2002-2026, City of Paris
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright notice
 *     and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above copyright notice
 *     and the following disclaimer in the documentation and/or other materials
 *     provided with the distribution.
 *
 *  3. Neither the name of 'Mairie de Paris' nor 'Lutece' nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * License 1.0
 */
package fr.paris.lutece.plugins.captcha.web;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.datastore.DatastoreService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.test.LuteceTestCase;
import fr.paris.lutece.test.mocks.MockHttpServletRequest;

/**
 * The captcha management panel changes the default engine with a POST form that carries a security token, checked on
 * the server
 */
public class CaptchaLutecePanelJspBeanTest extends LuteceTestCase
{
    private static final String DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE = "captcha.defaultProvider";
    private static final String ENGINE_BEFORE = "engineBefore";
    private static final String ENGINE_AFTER = TestCaptchaEngine.ENGINE_NAME;
    private static final Pattern FORM_TOKEN = Pattern.compile( "<form[^>]*method=\"post\"[\\s\\S]*?name=\"token\"[^>]*value=\"([^\"]+)\"" );

    private CaptchaLutecePanelJspBean _bean;
    private String _strDefaultEngine;

    @BeforeEach
    protected void setDefaultEngine( )
    {
        _bean = new CaptchaLutecePanelJspBean( );
        _strDefaultEngine = DatastoreService.getDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, null );
        DatastoreService.setDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, ENGINE_BEFORE );
    }

    @AfterEach
    protected void restoreDefaultEngine( )
    {
        if ( _strDefaultEngine == null )
        {
            DatastoreService.removeData( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE );
        }
        else
        {
            DatastoreService.setDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, _strDefaultEngine );
        }
    }

    /**
     * The token of the panel form, posted with the request, lets the default engine change once
     *
     * @throws AccessDeniedException
     *             if the token is refused
     */
    @Test
    public void testUpdateWithTheTokenOfThePanel( ) throws AccessDeniedException
    {
        MockHttpServletRequest request = newRequest( "POST" );
        Matcher matcher = FORM_TOKEN.matcher( _bean.getDashboardData( new AdminUser( ), request ) );
        assertTrue( matcher.find( ), "The panel must post its form with a security token" );
        request.addParameter( SecurityTokenService.PARAMETER_TOKEN, matcher.group( 1 ) );

        _bean.updateDefaultCaptcha( request );
        assertEquals( ENGINE_AFTER, DatastoreService.getDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, null ) );

        // A token is used once
        DatastoreService.setDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, ENGINE_BEFORE );
        assertThrows( AccessDeniedException.class, ( ) -> _bean.updateDefaultCaptcha( request ) );
        assertEquals( ENGINE_BEFORE, DatastoreService.getDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, null ) );
    }

    /**
     * Without a token, or with a token that was not issued, the default engine does not change
     */
    @Test
    public void testUpdateWithoutAValidTokenIsRefused( )
    {
        MockHttpServletRequest request = newRequest( "POST" );
        assertThrows( AccessDeniedException.class, ( ) -> _bean.updateDefaultCaptcha( request ) );

        request.addParameter( SecurityTokenService.PARAMETER_TOKEN, "unknown" );
        assertThrows( AccessDeniedException.class, ( ) -> _bean.updateDefaultCaptcha( request ) );

        assertEquals( ENGINE_BEFORE, DatastoreService.getDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, null ) );
    }

    /**
     * A GET request does not change the default engine, even with a valid token
     */
    @Test
    public void testUpdateWithAGetRequestIsRefused( )
    {
        MockHttpServletRequest request = newRequest( "GET" );
        Matcher matcher = FORM_TOKEN.matcher( _bean.getDashboardData( new AdminUser( ), request ) );
        assertTrue( matcher.find( ), "The panel must post its form with a security token" );
        request.addParameter( SecurityTokenService.PARAMETER_TOKEN, matcher.group( 1 ) );

        assertThrows( AccessDeniedException.class, ( ) -> _bean.updateDefaultCaptcha( request ) );
        assertEquals( ENGINE_BEFORE, DatastoreService.getDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, null ) );
    }

    /**
     * With a valid token, an engine that is not available is refused with a message, and the default engine does not
     * change
     *
     * @throws AccessDeniedException
     *             if the token is refused
     */
    @Test
    public void testUpdateWithAnUnknownEngineIsRefused( ) throws AccessDeniedException
    {
        for ( String strEngine : new String [ ] {
                "unknownEngine", ""
        } )
        {
            MockHttpServletRequest request = newRequest( "POST", strEngine );
            Matcher matcher = FORM_TOKEN.matcher( _bean.getDashboardData( new AdminUser( ), request ) );
            assertTrue( matcher.find( ), "The panel must post its form with a security token" );
            request.addParameter( SecurityTokenService.PARAMETER_TOKEN, matcher.group( 1 ) );

            assertTrue( _bean.updateDefaultCaptcha( request ).contains( "AdminMessage.jsp" ), "An unknown engine must be refused with a message" );
            assertEquals( ENGINE_BEFORE, DatastoreService.getDataValue( DATASTORE_KEY_DEFAULT_CAPTCHA_ENGINE, null ) );
        }
    }

    private static MockHttpServletRequest newRequest( String strMethod )
    {
        return newRequest( strMethod, ENGINE_AFTER );
    }

    private static MockHttpServletRequest newRequest( String strMethod, String strEngine )
    {
        MockHttpServletRequest request = new MockHttpServletRequest( );
        request.setMethod( strMethod );
        request.addParameter( "captcha_engine", strEngine );
        return request;
    }
}
