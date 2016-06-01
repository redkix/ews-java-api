/*
 * The MIT License
 * Copyright (c) 2012 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package microsoft.exchange.webservices.data.core.request;

import microsoft.exchange.webservices.data.core.ExchangeService;
import microsoft.exchange.webservices.data.core.enumeration.misc.TraceFlags;
import microsoft.exchange.webservices.data.core.exception.service.remote.ServiceRequestException;
import microsoft.exchange.webservices.data.misc.AsyncCallback;
import microsoft.exchange.webservices.data.misc.AsyncExecutor;
import microsoft.exchange.webservices.data.misc.AsyncRequestResult;
import microsoft.exchange.webservices.data.misc.CallableMethod;
import microsoft.exchange.webservices.data.misc.IAsyncResult;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * Defines the SimpleServiceRequestBase class.
 */
public abstract class SimpleServiceRequestBase<T> extends ServiceRequestBase<T> {

  /**
   * Initializes a new instance of the SimpleServiceRequestBase class.
   */
  protected SimpleServiceRequestBase(ExchangeService service)
      throws Exception {
    super(service);
  }

  /**
   * Executes this request.
   *
   * @return response object
   * @throws Exception on error
   */
  protected T internalExecute() throws Exception {
    HttpWebRequest response = null;

    try {
      response = this.validateAndEmitRequest();
      return this.readResponse(response, ServiceRequestBase.getResponseStream(response));
    } catch (XMLStreamException e) {
      // Guy: My code is this function only
      return handleParseErrorException(e, response);
    } catch (IOException ex) {
      // Wrap exception.
      throw new ServiceRequestException(String.
          format("The request failed. %s", ex.getMessage()), ex);
    } catch (Exception e) {
      if (response != null) {
        this.getService().processHttpResponseHeaders(TraceFlags.
            EwsResponseHttpHeaders, response);
      }

      throw new ServiceRequestException(String.format("The request failed. %s", e.getMessage()), e);
    }
  }

  private T handleParseErrorException(Exception e, HttpWebRequest response) throws Exception {
    if (e.getMessage().contains("ParseError at")) {
      // Guy: We will get this exception if the xml will contain non-valid characters.
      // Clients can send emails with non-valid characters but EWS library will pass them as-is - without cleaning/replacing
      // them. To solve this, we will make another request and we will wrap the response InputStream with our ReplacingInputStream
      // which will remove (maybe at the future we will do replacements) the non-valid characters.
      try {
        response = this.validateAndEmitRequest();
        InputStream is = ServiceRequestBase.getResponseStream(response);
        is = new ReplacingInputStream(is);
        return this.readResponse(response, is);
      } catch (IOException ex) {
        // Wrap exception.
        throw new ServiceRequestException(String.
            format("The request failed. %s", ex.getMessage()), ex);
      } catch (Exception ex) {
        e = ex;
      }
    }

    if (response != null) {
      this.getService().processHttpResponseHeaders(TraceFlags.
                                                       EwsResponseHttpHeaders, response);
    }

    throw new ServiceRequestException(String.format("The request failed. %s", e.getMessage()), e);
  }

  /**
   * Ends executing this async request.
   *
   * @param asyncResult The async result
   * @return Service response object
   * @throws Exception on error
   */
  protected T endInternalExecute(IAsyncResult asyncResult) throws Exception {
    HttpWebRequest response = (HttpWebRequest) asyncResult.get();
    return this.readResponse(response, response.getInputStream());
  }

  /**
   * Begins executing this async request.
   *
   * @param callback The AsyncCallback delegate.
   * @return An IAsyncResult that references the asynchronous request.
   * @throws Exception on error
   */
  public AsyncRequestResult beginExecute(AsyncCallback callback) throws Exception {
    this.validate();

    HttpWebRequest request = this.buildEwsHttpWebRequest();
    AsyncExecutor es = new AsyncExecutor();
    Callable<?> cl = new CallableMethod(request);
    Future<?> task = es.submit(cl, callback);
    es.shutdown();

    return new AsyncRequestResult(this, request, task, null);
  }

}
