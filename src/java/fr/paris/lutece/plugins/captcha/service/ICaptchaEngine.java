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
package fr.paris.lutece.plugins.captcha.service;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Interface for captcha engine implementations
 */
public interface ICaptchaEngine
{
    /**
     * Validate the captcha field
     * 
     * @param request
     *            The HTTP request
     * @return True if OK, otherwise false
     */
    boolean validate( HttpServletRequest request );

    /**
     * Gets the captcha HTML code
     * <p>
     * The returned string is HTML rendered by the engine, every value escaped there. Under the always-on HTML
     * auto-escaping of FreeMarker (LUT-33153), a String put in a model prints as text: the consumer that puts it in its
     * model wraps it with {@code HtmlMarkup.of( strHtml )} and writes it {@code ${captcha}}, never with {@code ?no_esc}.
     * </p>
     * 
     * @return The captcha HTML code, to be wrapped with {@code HtmlMarkup.of} by the consumer
     */
    String getHtmlCode( );

    /**
     * Get the name of the captcha engine
     * 
     * @return The name of the captcha engine
     */
    String getCaptchaEngineName( );
}
