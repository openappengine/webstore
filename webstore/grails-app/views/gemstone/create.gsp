<%@ page import="com.openappengine.model.product.Gemstone" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="admin">
		<g:set var="entityName" value="${message(code: 'prodGemstone.label', default: 'ProdGemstone')}" />
		<title>
			Create New Gemstone
		</title>
	</head>
	<body>
		<div class="row">
			<div class="elevencol">
				<h1 class="page-subtitle">
					New Gemstone
				</h1>
				<hr/>
			</div>
		</div>
		
		
		<div class="row">
			<div class="elevencol">
				<div id="create-prodGemstone" class="content scaffold-create" role="main">
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
							<g:submitButton name="create" class="button" 
								value="Save" />
						</div>
					</g:form>
				</div>
			</div>
		</div>
	</body>
</html>
