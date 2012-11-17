<%@ page import="com.openappengine.model.product.Gemstone" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<g:set var="entityName" value="${message(code: 'prodGemstone.label', default: 'ProdGemstone')}" />
		<title><g:message code="default.create.label" args="[entityName]" /></title>
	</head>
	<body>
		<div id="create-prodGemstone" class="content scaffold-create" role="main">
			<h1 class="page-title">
				New Gemstone
			</h1>
			<g:if test="${flash.message}">
			<div class="message" role="status">${flash.message}</div>
			</g:if>
			<g:hasErrors bean="${prodGemstoneInstance}">
			<ul class="errors" role="alert">
				<g:eachError bean="${prodGemstoneInstance}" var="error">
				<li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message error="${error}"/></li>
				</g:eachError>
			</ul>
			</g:hasErrors>
			<g:form action="save" >
				<div>
					<g:render template="form"/>
					<g:submitButton name="create" class="save" 
						value="Save" />
				</div>
			</g:form>
		</div>
	</body>
</html>