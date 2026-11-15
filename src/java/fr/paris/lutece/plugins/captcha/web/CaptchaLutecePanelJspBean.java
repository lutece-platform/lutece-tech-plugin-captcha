/*
 * Copyright (c) 2002-2021, City of Paris
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

import fr.paris.lutece.plugins.captcha.service.CaptchaService;
import fr.paris.lutece.portal.business.user.AdminUser;
import fr.paris.lutece.portal.service.admin.AccessDeniedException;
import fr.paris.lutece.portal.service.admin.AdminUserService;
import fr.paris.lutece.portal.service.captcha.ICaptchaService;
import fr.paris.lutece.portal.service.message.AdminMessage;
import fr.paris.lutece.portal.service.message.AdminMessageService;
import fr.paris.lutece.portal.service.security.ISecurityTokenService;
import fr.paris.lutece.portal.service.security.SecurityTokenService;
import fr.paris.lutece.portal.service.template.AppTemplateService;
import fr.paris.lutece.portal.service.util.AppPathService;
import fr.paris.lutece.portal.service.dashboard.admin.AdminDashboardComponent;
import fr.paris.lutece.util.html.HtmlTemplate;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.inject.literal.NamedLiteral;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Panel for global management page. Its name, under which the core stores its placement, is the one declared in the
 * plugin file (captchaAdminDashboardComponent); the panel template shows the translated title
 */
public class CaptchaLutecePanelJspBean extends AdminDashboardComponent
{
    /**
     * generated serial version UID
     */
    private static final long serialVersionUID = 8792952492908177294L;

    private static final String PARAMETER_CAPTCHA_ENGINE = "captcha_engine";
    private static final String METHOD_POST = "POST";

    // Key of the security token of the form that changes the default engine
    private static final String ACTION_UPDATE_DEFAULT_CAPTCHA = "updateDefaultCaptcha";
    private static final String MESSAGE_INVALID_TOKEN = "Invalid security token";
    private static final String MESSAGE_UNKNOWN_CAPTCHA_ENGINE = "captcha.globalmanagement.message.unknownCaptchaEngine";

    private static final String MARK_CURRENT_CAPTCHA_ENGINE = "current_captcha_engine";
    private static final String MARK_LIST_CAPTCHA_ENGINE = "listCaptchaEngine";

    private static final String TEMPLATE_CAPTCHA_PANEL = "admin/plugins/captcha/panel_captcha_management.html";

    private ICaptchaService _captchaService;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDashboardData( AdminUser user, HttpServletRequest request )
    {
        ICaptchaService captchaService = getCaptchaService( );
        Map<String, Object> model = new HashMap<>( );
        model.put( MARK_CURRENT_CAPTCHA_ENGINE, captchaService.getDefaultCaptchaEngineName( ) );
        model.put( MARK_LIST_CAPTCHA_ENGINE, captchaService.getCaptchaEngineNameList( ) );
        model.put( SecurityTokenService.MARK_TOKEN, getSecurityTokenService( ).getToken( request, ACTION_UPDATE_DEFAULT_CAPTCHA ) );

        HtmlTemplate template = AppTemplateService.getTemplate( TEMPLATE_CAPTCHA_PANEL, AdminUserService.getLocale( request ), model );

        return template.getHtml( );
    }

    /**
     * Returns the panel's order. This panel is a second panel.
     * 
     * @return 2
     */
    @Override
    public int getOrder( )
    {
        return 2;
    }

    /**
     * Update the default captcha to use. The request must be a POST that carries the security token of the panel form,
     * and the engine must be one of the available engines
     * 
     * @param request
     *            The request
     * @return The next URL to redirect to, or the URL of an error message if the engine is not available
     * @throws AccessDeniedException
     *             if the request is not a POST or its security token is not valid
     */
    public String updateDefaultCaptcha( HttpServletRequest request ) throws AccessDeniedException
    {
        if ( !METHOD_POST.equalsIgnoreCase( request.getMethod( ) ) || !getSecurityTokenService( ).validate( request, ACTION_UPDATE_DEFAULT_CAPTCHA ) )
        {
            throw new AccessDeniedException( MESSAGE_INVALID_TOKEN );
        }
        String strDefaultCaptcha = request.getParameter( PARAMETER_CAPTCHA_ENGINE );
        ICaptchaService captchaService = getCaptchaService( );
        if ( !captchaService.getCaptchaEngineNameList( ).contains( strDefaultCaptcha ) )
        {
            return AdminMessageService.getMessageUrl( request, MESSAGE_UNKNOWN_CAPTCHA_ENGINE, AdminMessage.TYPE_STOP );
        }
        captchaService.setDefaultCaptchaEngineName( strDefaultCaptcha );
        return AppPathService.getBaseUrl( request );
    }

    /**
     * Get the security token service
     * 
     * @return the security token service
     */
    private static ISecurityTokenService getSecurityTokenService( )
    {
        return CDI.current( ).select( ISecurityTokenService.class ).get( );
    }

    /**
     * Get a captcha service instance
     * 
     * @return An instance of the captcha servcie
     */
    private ICaptchaService getCaptchaService( )
    {
        if ( _captchaService == null )
        {
            _captchaService = CDI.current( ).select( ICaptchaService.class, NamedLiteral.of( CaptchaService.BEAN_NAME ) ).get( );
        }
        return _captchaService;
    }
}
