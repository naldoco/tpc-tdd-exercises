/*
 * Copyright (C) 2010-2011, Pedro Ballesteros <pedro@theprogrammingchronicles.com>
 *
 * This file is part of The Programming Chronicles Test-Driven Development
 * Exercises(http://theprogrammingchronicles.com/)
 *
 * This copyrighted material is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This material is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this material. This copy is available in LICENSE-GPL.txt
 * file. If not, see <http://www.gnu.org/licenses/>.
 */

package com.programmingchronicles.tdd.web.addressbook;

import com.programmingchronicles.tdd.addressbook.GlobalAddressBook;
import com.programmingchronicles.addressbook.converters.ContactCommandConverter;
import java.io.IOException;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Controlador que procesa el formulario de creación de un contacto.
 *
 * Se implementa el código de inicialización que no puede estar basado
 * en Direct Injection, aunque se sigue manteniendo configurable.
 *
 * FUTURA REFACTORIZACION:
 *    Implementar una arquitectura MVC de Controlador Frontal de forma
 *    que los controladores no tengan que ser servlets.
 *
 * DISEÑO: Se usa un patrón clásico de procesamiento de formularios
 *    El HTTP GET devuelve el formulario.
 *    El HTTP POST procesa el formulario.
 *       - Si OK devuelve un redirect al navegador, evitando el doble POST.
 *       - Si Error devuelve de nuevo el formulario indicando los campos erroneos.
 *
 * @author Pedro Ballesteros <pedro@theprogrammingchronicles.com>
 */
public class AddContactController extends HttpServlet {

    // Configuración de las vistas y redirecciones.
    private String successedSubmitRedirect;
    private String formView;

    // Acceso al servicio de GlobalAddressBook, se configurará
    // mediante Direct Injection (IoC).
    private GlobalAddressBook addressBook;

    /**
     * En los objetos instanciados por el contenedor no se puede usar Direct
     * Injection, para configurar las dependencias de los controladores.
     *
     * Al implementarse como servlets las instancia el contenedor.
     *
     * Se pierde parte de IoC, pero aun así se implementa un mecanismo que
     * que sigue manteniendo los controladores independientes de la 
     * implementación concreta de los servicios.
     * 
     * FUTURA REFACTORIZACION:
     *   Implementar una arquitectura MVC de Controlador Frontal de forma
     *   que los controladores no tengan que ser servlets.
     *
     * @author Pedro Ballesteros <pedro@theprogrammingchronicles.com>
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        addressBook = (GlobalAddressBook) config.getServletContext().getAttribute("globalAddressBook");
        formView = config.getInitParameter("formView");
        successedSubmitRedirect = config.getInitParameter("successedSubmitRedirect");
    }


    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        request.getRequestDispatcher(formView).forward(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
       // REFACTORIZACION:
       //
       // La funcionalidad de extraer, convertir y validar parametros se lleva
       // a una clase con esa responsabilidad.
       ContactCommandConverter converter = new ContactCommandConverter();

       if(converter.verifyAndConvert(request)) {
           // Se inserta el nuevo contacto en la agenda.
           addressBook.addContact(converter.getCommand());

           // Envia una respuesta de redirección al navegador (evitando el doble post).
          response.sendRedirect(successedSubmitRedirect);

        } else {
           // Si hay errores se publican en el modelo para que la vista los
           // pueda mostrar.
           request.setAttribute("errors", converter.getErrors());

           // En caso de error la petición se envía al mismo form para que el usuario
           // vea los errores y pueda corregirlos.
           request.getRequestDispatcher(formView).forward(request, response);
        }
    }

    /**
     * Obtiene el servicio de agenda configurado.
     *
     * @return
     */
    public GlobalAddressBook getAddressBook() {
        return addressBook;
    }

    /**
     * Configura el servicio de agenda.
     *
     * @param addressBook
     */
    public void setAddressBook(GlobalAddressBook addressBook) {
        this.addressBook = addressBook;
    }

    /**
     * Obtiene el path de la redirección de submit correcto.
     *
     * @return
     */
    public String getSuccessedSubmitRedirect() {
        return successedSubmitRedirect;
    }

    /**
     * Configura el path de la redirección de submit correcto.
     *
     * @param successedSubmitRedirect
     */
    public void setSuccessedSubmitRedirect(String successedSubmitRedirect) {
        this.successedSubmitRedirect = successedSubmitRedirect;
    }

    /**
     * Obtiene el path de la vista del formulario.
     *
     * @return
     */
    public String getFormView() {
        return formView;
    }

    /**
     * Configura el path de la vista del formulario.
     *
     * @param formView
     */
    public void setFormView(String formView) {
        this.formView = formView;
    }
}