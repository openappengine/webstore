
<%@ page import="com.openappengine.model.product.Jewellery" %>
<!doctype html>
<html>
	<head>
		<meta name="layout" content="main">
		<title>
			${prodJewelleryInstance?.pdProductName}
		</title>
		
		<script>
			$(function() {
				$("#tabs").tabs({
					ajaxOptions: {
						error: function( xhr, status, index, anchor ) {
							$( anchor.hash ).html(
								"Couldn't load this tab. We'll try to fix this as soon as possible.");
						}
					},
					load: function(event, ui) {
						
				    }		
				});
			});
		</script>
		
		<style type="text/css">
			.ui-tabs-nav {
				background-color: #fafafa;
				border:0px;
				border-bottom: 1px solid #D9DCDC;
				font-family:'Droid Sans',Tahoma,Arial,sans-serif;
				font-size: 12px;
				text-transform:uppercase; 
			}
			
			.ui-tabs {
				font-family:'Droid Sans',Tahoma,Arial,sans-serif;
				font-size: 12px;
				border-radius: 1px;
			}
			
			.ui-tabs-panel {
				border-top: 0px;
				border-bottom: 1px solid #D9DCDC;
				border-left: 1px solid #D9DCDC;
				border-right: 1px solid #D9DCDC;
				font-family:'Droid Sans',Tahoma,Arial,sans-serif;
				font-size: 13px;
				height: 580px;
			    overflow: auto;
			}
			
			.ui-tabs-panel > * {
				font-size: 12px;
			}
		</style>
	</head>
	<body>
		<div class="row" style="height: 250px;">
			<!-- DETAIL IMAGE -->
			<div class="fivecol" style="margin-top: -1.45em;margin-right:18px;">
				<div class="productDetImageBox">
					<g:render template="/common/product/detailImageBox" model="[productInstance:prodJewelleryInstance]"></g:render>
				</div>
				<p style="text-align: right;">
					Rollover to zoom
				</p>
			</div>
			<!-- DETAIL IMAGE -->
			
			<!-- INFO-->
			<div class="fivecol details" style="margin-left: -1.5em; margin-top: 0.9em;">
				<sec:ifAnyGranted roles="ROLE_ADMIN,SITE_ADMIN">
				  <p align="right">
					  <g:link class="edit" mapping="jewelleryEdit" params="[id : prodJewelleryInstance?.pdProductId]">Edit</g:link>
				  </p>
				</sec:ifAnyGranted>
				<div id="content">
					<h1 class="product_name_header">
						${prodJewelleryInstance?.pdProductName}
					</h1>
					
					<p>
					<%=prodJewelleryInstance?.pdDescription %>
					</p>
					
					<br/>
					
					<table style="margin-top:5px;">
						<tr>
							<td style="width:120px;">Overall Rating</td>
							<td style="width:90px;"><span id="overall_Det"> </span></td>
							<td>(${prodJewelleryInstance?.calculatedInfo?.averageCustomerRating}
								OUT OF 5)
							</td>
						</tr>
						<tr>
							<td colspan="2"><a href="#tabs" onclick="$('#tabs').tabs('select', 1);">
									Ratings & Reviews
									(${prodJewelleryInstance?.productReviews?.size()}) </a></td>
						</tr>
					</table>
					
					<g:hiddenField name="overall_Det_Rating" value="${prodJewelleryInstance?.calculatedInfo?.averageCustomerRating}" />
					<script>
						$(function() {
							$('#overall_Det').ratings(5, $('#overall_Det_Rating').val(), true);
						});
					</script>	
	
					<div class="product_reviews" style="margin-top: 15px; margin-left:-10px;"></div>
	
					<div class="product_price" style="margin-top: 5px;">
						Price : <strong> $ <g:formatNumber
								number="${prodJewelleryInstance?.getProductPrice(new Date())}"
								maxFractionDigits="2" />
						</strong>
				   </div>
				   
					<div style="margin-top: 15px;">
						<g:form name="add_to_cart_form" action="addToShoppingCart" controller="shoppingCart" >
							<g:hiddenField name="quantityOrdered" value="1"/>
							<g:hiddenField name="productId" value="${prodJewelleryInstance?.pdProductId}"/>
							<g:submitButton class="button" name="addToCart" value="Add To Cart"/>
						</g:form>
					</div>
					
					<br/>
					<hr style="margin:5px;"/>

					<div id="links" style="margin-left: -15px;">
						<ul class="productDetQuickLinks">
							<li class="add_to_wishList">
								<g:remoteLink controller="wishList" action="addToWishList" 
										params="[productId : prodJewelleryInstance?.pdProductId]"
										onSuccess="updateWishListMessage();">
									<img src="${resource(dir: '/images/site', file:'icon_wishlist.gif')}" style="width:16px;height:16px;"/>
									Add to WishList
								</g:remoteLink>
							</li>
							
							<li class="email_a_friend">
								<a href="#">
									<img src="${resource(dir: '/images/site', file:'icon_mail.gif')}" style="width:16px;height:16px;"/>
									Email a Friend
								</a>
							</li>
						</ul>
					</div>
					
				</div>
			</div>
		</div>
		<!-- INFO-->
		
		<!-- Tabs -->
		<div id="detailsInfo" class="row" >
			<div class="elevencol" style = "padding-top: 20px;">
				<div id="tabs">
					<ul>
						<li>
							<g:link controller="Jewellery" action="viewFeatures" id="${prodJewelleryInstance?.pdProductId}">
								Details
							</g:link>
						</li>
						<li>
							<g:link controller="productReview" action="_productReviews" params="[productId:prodJewelleryInstance?.pdProductId]">
								Reviews (${prodJewelleryInstance?.productReviews?.size()})
							</g:link>
						</li>
						
						<!-- Issue #8  -->
<%--						<g:if test="${prodJewelleryInstance?.certified}">--%>
<%--							<li><g:link controller="Jewellery" action="certificate" >--%>
<%--								Certificates--%>
<%--							</g:link></li>--%>
<%--						</g:if>--%>
						
						<li><g:link controller="Jewellery" action="shipping" >
								SHIPPING AND PACKAGING
							</g:link></li>
					</ul>
				</div>
			</div>
		</div>
		<script type="text/javascript">
			$("#wishListLink").live('click', function(e) {
				  alert('jo');
				  $.ajax({
				        type: 'POST',
				        async : true,
				        url: this.href + "?productId=" + this.attr('rel') , 
				        success: function(data) {
				        	$('#wishListItems').html("(" + data + ")");
				        }
				  });
				  e.preventDefault();	
			});
		</script>
	</body>
</html>