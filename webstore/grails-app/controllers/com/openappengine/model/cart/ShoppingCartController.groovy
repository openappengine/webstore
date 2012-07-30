package com.openappengine.model.cart

import org.grails.paypal.Payment
import org.grails.paypal.PaymentItem
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.web.context.request.RequestContextHolder

import com.openappengine.model.product.Product

class ShoppingCartController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [shoppingCartInstanceList: ShoppingCart.list(params), shoppingCartInstanceTotal: ShoppingCart.count()]
    }

    def create() {
        [shoppingCartInstance: new ShoppingCart(params)]
    }
	
	def createShoppingCart() {
		def sessionID = RequestContextHolder.getRequestAttributes()?.getSessionId()
		
		def sc = ShoppingCart.findBySessionID(sessionID)
		if(!sc) {
			sc = new ShoppingCart(sessionID:sessionID)
			sc.save(flush:true)
		}
	}
	
	def addToShoppingCart() {
		def sessionID = RequestContextHolder.getRequestAttributes()?.getSessionId()
		
		def sc = ShoppingCart.findBySessionID(sessionID)
		if(!sc) {
			sc = new ShoppingCart(sessionID:sessionID)
			sc.save(flush:true)
		}
		
		def sci= ShoppingCartItem.findByShoppingCartAndProductId(sc,params.productId)
		if(!sci) {
			sci = new ShoppingCartItem()
			sci.shoppingCart = sc
			
			sci.productId = params.productId
			sci.quantity = 1
			BigDecimal price = Product.get(params.productId)?.getProductPrice(new Date())
			sci.lineTotalPrice = price?.multiply(sci.quantity)
		} else {
			sci.quantity = sci.quantity + 1;
			
			BigDecimal price = Product.get(params.productId)?.getProductPrice(new Date())
			sci.lineTotalPrice = price?.multiply(sci.quantity)
		}
		
		sci.save(flush:true)
		
		
		redirect(action: "showCart",id:sc.shoppingCartId)
	}
	
	def showCart() {
		def shoppingCartInstance = ShoppingCart.get(params.id)
		if (!shoppingCartInstance) {
			//TODO
			return
		}

		[shoppingCartInstance: shoppingCartInstance]
	}
	
	def checkoutPaypal() {
		if(!params.shoppingCartId) {
			//TODO
			return
		}
		
		def sc = ShoppingCart.get(params.shoppingCartId)
		Payment paypalPayment = null
		if(sc.cartItems) {
			paypalPayment = new Payment()
			paypalPayment.buyerId = 0;
			paypalPayment.discountCartAmount = new BigDecimal("0.0")
			paypalPayment.tax = new BigDecimal("0.0")
			
			sc.cartItems.eachWithIndex { cartItem, i ->
				def ppi = new PaymentItem()
				ppi.amount = cartItem.lineTotalPrice
				ppi.itemName = Product.get(cartItem.productId)?.pdProductName
				ppi.itemNumber = cartItem.productId
				ppi.quantity = cartItem.quantity
				
				paypalPayment.addToPaymentItems(ppi)
			}
		}
		paypalPayment.save(flush:true)
		redirect(controller:"paypal",action:"uploadCart",params:[transactionId:paypalPayment.transactionId])
	}
	
    def save() {
        def shoppingCartInstance = new ShoppingCart(params)
        if (!shoppingCartInstance.save(flush: true)) {
            render(view: "create", model: [shoppingCartInstance: shoppingCartInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), shoppingCartInstance.id])
        redirect(action: "show", id: shoppingCartInstance.id)
    }

    def show() {
        def shoppingCartInstance = ShoppingCart.get(params.id)
        if (!shoppingCartInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), params.id])
            redirect(action: "list")
            return
        }

        [shoppingCartInstance: shoppingCartInstance]
    }

    def edit() {
        def shoppingCartInstance = ShoppingCart.get(params.id)
        if (!shoppingCartInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), params.id])
            redirect(action: "list")
            return
        }

        [shoppingCartInstance: shoppingCartInstance]
    }

    def update() {
        def shoppingCartInstance = ShoppingCart.get(params.id)
        if (!shoppingCartInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (shoppingCartInstance.version > version) {
                shoppingCartInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'shoppingCart.label', default: 'ShoppingCart')] as Object[],
                          "Another user has updated this ShoppingCart while you were editing")
                render(view: "edit", model: [shoppingCartInstance: shoppingCartInstance])
                return
            }
        }

        shoppingCartInstance.properties = params

        if (!shoppingCartInstance.save(flush: true)) {
            render(view: "edit", model: [shoppingCartInstance: shoppingCartInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), shoppingCartInstance.id])
        redirect(action: "show", id: shoppingCartInstance.id)
    }

    def delete() {
        def shoppingCartInstance = ShoppingCart.get(params.id)
        if (!shoppingCartInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), params.id])
            redirect(action: "list")
            return
        }

        try {
            shoppingCartInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'shoppingCart.label', default: 'ShoppingCart'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}
