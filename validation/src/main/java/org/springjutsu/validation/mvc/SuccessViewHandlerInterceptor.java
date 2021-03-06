/*
 * Copyright 2010-2013 Duplichien, Wicksell, Springjutsu.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springjutsu.validation.mvc;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springjutsu.validation.mvc.annotations.SuccessView;
import org.springjutsu.validation.mvc.annotations.SuccessViews;
import org.springjutsu.validation.util.RequestUtils;

/**
 * Handles the implementation of the @link{SuccessView} annotation.
 * Self-registering bean that places itself at the end of the interceptor
 * chain on a handler adapter.
 * @author Clark Duplichien
 *
 */
public class SuccessViewHandlerInterceptor extends HandlerInterceptorAdapter {
	
	/**
	 * Responsible for redirecting to the view defined
	 * in the @link{SuccessView} annotation on the current
	 * handler method.
	 * In the event that there are multiple possible 
	 * success paths defined for the success view, 
	 * will attempt to isolate the closest matching
	 * url-path key and redirect to the value path,
	 * as described in @link{SuccessView}
	 */
	@Override
	public void postHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler,
			ModelAndView modelAndView) {
		
		if (!(handler instanceof HandlerMethod)) {
			return;
		}
		
		SuccessViews successViews = (SuccessViews) ((HandlerMethod) handler).getMethodAnnotation(SuccessViews.class);
		SuccessView successView = (SuccessView) ((HandlerMethod) handler).getMethodAnnotation(SuccessView.class);
		String viewName = null;

		if (successView != null) {
			viewName = successView.value();
		} else if (successViews != null) {
			String[] controllerPaths = RequestUtils.getControllerRequestPaths((HandlerMethod) handler);
			viewName = findMatchingTargetUrl(successViews.value(), controllerPaths, request);
		}
				
		if (viewName == null) {
			return;
		}
		
		viewName = RequestUtils.replaceRestPathVariables(viewName, modelAndView.getModel(),  request);
		modelAndView.setViewName(viewName);
	}
	
	/**
	 * Given multiple possible view candidates, find the one whose sourceUrl
	 * pattern best matches the current request url.
	 * @param successViews An array of SuccessView candidate objects
	 * @param controllerPaths RequestMapping mapped base paths for the controller
	 * @param request The current request
	 * @throws IllegalArgumentException if any SuccessView annotation does not
	 * provide a sourceUrl value, or if multiple SuccessView annotations provide
	 * the same sourceUrl value 
	 * @return The view name mapped by the best matching SuccessView annotation
	 */
	protected String findMatchingTargetUrl(SuccessView[] successViews, String[] controllerPaths, HttpServletRequest request) {
		Map<String, String> sourceTargetMap = new HashMap<String, String>();
		String[] sources = new String[successViews.length];
		for (int i = 0; i < successViews.length; i++) {
			SuccessView successView = successViews[i];
			
			if (successView.sourceUrl().isEmpty()) {
				throw new IllegalArgumentException("sourceUrl is required when specifying multiple success or failure views.");
			}
			
			if (sourceTargetMap.containsKey(successView.sourceUrl())) {
				throw new IllegalArgumentException("duplicate sourceUrl when specifying multiple success or failure views: " + successView.sourceUrl());
			}
			
			sourceTargetMap.put(successView.sourceUrl(), successView.value());
			sources[i] = successView.sourceUrl();
		}
		
		String matchingSourceUrl = RequestUtils.findFirstMatchingRestPath(sources, controllerPaths, request);
		
		return matchingSourceUrl == null ? null : sourceTargetMap.get(matchingSourceUrl);		
	}
}
