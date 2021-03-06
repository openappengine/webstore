package com.openappengine.product

import org.springframework.dao.DataIntegrityViolationException

import com.openappengine.model.product.Product
import com.openappengine.model.product.ProductCategory
import com.openappengine.model.product.ProductType

class ProductController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [productInstanceList: Product.list(params), productInstanceTotal: Product.count()]
    }

    def create() {
        [productInstance: new Product(params)]
    }

    def save() {
        def productInstance = new Product(params)
        if (!productInstance.save(flush: true)) {
            render(view: "create", model: [productInstance: productInstance])
            return
        }

		flash.message = message(code: 'default.created.message', args: [message(code: 'product.label', default: 'Product'), productInstance.id])
        redirect(action: "show", id: productInstance.id)
    }

    def show() {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])
            redirect(action: "list")
            return
        }

        [productInstance: productInstance]
    }

    def edit() {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])
            redirect(action: "list")
            return
        }

        [productInstance: productInstance]
    }

    def update() {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (productInstance.version > version) {
                productInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'product.label', default: 'Product')] as Object[],
                          "Another user has updated this Product while you were editing")
                render(view: "edit", model: [productInstance: productInstance])
                return
            }
        }

        productInstance.properties = params

        if (!productInstance.save(flush: true)) {
            render(view: "edit", model: [productInstance: productInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'product.label', default: 'Product'), productInstance.id])
        redirect(action: "show", id: productInstance.id)
    }

    def delete() {
        def productInstance = Product.get(params.id)
        if (!productInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'product.label', default: 'Product'), params.id])
            redirect(action: "list")
            return
        }

        try {
            productInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'product.label', default: 'Product'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'product.label', default: 'Product'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
	
	def getProductPrice() {
		def qty = params.quantity
		if(!qty) {
			render "Nan"
			return;
		}
		
		def product = Product.get(params.productId)
		if(product) {
			def price = product.getProductPrice(new Date())
			if(price) {
				render price.multiply(qty.toBigDecimal());
			} else {
				render "0"
			}
		}
	}
	
	def ajaxGetCat1ForProductType() {
		def productType = params.productTypeId
		def parentCat = ProductType.get(params.productTypeId)
		def cat1s = ProductType.findAllByParentType(parentCat)
		render(template:"/gemstone/productCats", model: [cat1s:cat1s])
	}
	
	def ajaxGetCategoryTypes() {
		def a= params
		if(!params.productCategory) {
			//TODO
			return
		}
		
		def productCategory = ProductCategory.findByProductCategoryName(params.productCategory)
		
		def productTypes = new ArrayList<ProductType>()
		if(productCategory) {
			productTypes.addAll(productCategory?.productTypes)
		}
		
		def model = [productTypes: productTypes]
		if (request.xhr) {
				// ajax request
			if(params.template) {
				render(template:params.template, model: model)
			} else {
				render(template: "/gemstone/productTypes", model: model)
			}
		} else {
			model
		}
	}
	
	def showModalGallery() {
		render(view: "/common/gallery")
	}
	
	def ajaxGetCat1() {
		def a = params
		if(!params.productCategory) {
			//TODO

		
		}
		
		def productCat1 = params.productCategory
		
		//def productTypeURL = "Star Rubies".asFriendlyUrl()
		def parentCat = ProductType.findByProductTypeName(productCat1)
		def productTypes = new ArrayList<ProductType>()
		productTypes = ProductType.findAllByParentType(parentCat)
		//if(productCategory) {
		//	productTypes.addAll(productCat1)
		//}
		
		def model = [productTypes: productTypes, productCat1:productCat1]
		if (request.xhr) {
				// ajax request
			
				render(template:params.template, model: model)
			
		} 
		
	}
}
