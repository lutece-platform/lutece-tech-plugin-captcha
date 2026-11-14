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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;

/**
 * Renders the captcha management panel with HTML auto-escaping (LUT-33153), the configuration the core builds: the
 * engine names are written as text, and the default engine stays selected.
 */
public class CaptchaPanelTemplateRenderTest
{
    private static final String TEMPLATES_ROOT = "webapp/WEB-INF/templates";
    private static final String TEMPLATE_CAPTCHA_PANEL = "admin/plugins/captcha/panel_captcha_management.html";

    /**
     * Renders the panel.
     *
     * @param strCurrent
     *            the default engine, or null
     * @param listEngines
     *            the engine names
     * @return the HTML
     * @throws Exception
     *             if the template does not render
     */
    private static String render( String strCurrent, List<String> listEngines ) throws Exception
    {
        Configuration cfg = new Configuration( Configuration.VERSION_2_3_32 );
        cfg.setOutputFormat( HTMLOutputFormat.INSTANCE );
        cfg.setAutoEscapingPolicy( Configuration.ENABLE_IF_DEFAULT_AUTO_ESCAPING_POLICY );
        cfg.setDirectoryForTemplateLoading( new File( TEMPLATES_ROOT ) );

        Map<String, Object> model = new HashMap<>( );
        if ( strCurrent != null )
        {
            model.put( "current_captcha_engine", strCurrent );
        }
        model.put( "listCaptchaEngine", listEngines );
        model.put( "token", "token" );

        Template template = cfg.getTemplate( TEMPLATE_CAPTCHA_PANEL );
        StringWriter writer = new StringWriter( );
        template.process( model, writer );
        return writer.toString( );
    }

    /**
     * An engine name holding markup characters is written as text, in the value and in the label of its option.
     *
     * @throws Exception
     *             if the template does not render
     */
    @Test
    public void testEngineNamesAreWrittenAsText( ) throws Exception
    {
        String strHtml = render( "jcaptcha", List.of( "jcaptcha", "<b>\"engine\"</b>" ) );

        assertTrue( strHtml.contains( "<option value=\"&lt;b&gt;&quot;engine&quot;&lt;/b&gt;\" >&lt;b&gt;&quot;engine&quot;&lt;/b&gt;</option>" ),
                strHtml );
        assertFalse( strHtml.contains( "<b>" ), strHtml );
    }

    /**
     * The default engine is selected; without a default engine, the panel still renders and selects none.
     *
     * @throws Exception
     *             if the template does not render
     */
    @Test
    public void testDefaultEngineIsSelected( ) throws Exception
    {
        assertTrue( render( "recaptcha", List.of( "jcaptcha", "recaptcha" ) ).contains( "<option value=\"recaptcha\" selected=\"selected\">recaptcha</option>" ) );
        assertFalse( render( null, List.of( "jcaptcha", "recaptcha" ) ).contains( "selected=\"selected\"" ) );
    }
}
