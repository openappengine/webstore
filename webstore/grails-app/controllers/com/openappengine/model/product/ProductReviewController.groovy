package com.openappengine.model.product

import org.springframework.dao.DataIntegrityViolationException

class ProductReviewController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index() {
        redirect(action: "list", params: params)
    }

    def list() {
        params.max = 3
        [productReviewInstanceList: ProductReview.list(params), productReviewInstanceTotal: ProductReview.count()]
    }
	
	def _productReviews() {
		if(!params.productId) {
			//TODO
		}
		params.max = 3
		params.offset = params.offset?params.offset:0
		 
		Product product = Product.get(params.productId)
		def c = ProductReview.createCriteria()
		def results = c.list {
			like("product", product)
			order("postedDate", "desc")
			firstResult(params.offset.toInteger())
			maxResults(params.max.toInteger())
		}
		
		int reviews = ProductReview.countByProduct(product)
		
		def model = [productReviewInstanceList: results, productReviewInstanceTotal: reviews]

		//model =  [productReviewInstanceList: ProductReview.list(params), productReviewInstanceTotal: ProductReview.count()]
		
		params.productId = product?.pdProductId
		
		if (request.xhr) {
			// ajax request
			render(template: "productReviews", model: model)
		} else {
			model
		}
	}

    def create() {
		def productReview = new ProductReview(params);
		if(params.productId) {
			def product = Product.get(params.productId)
			productReview.product = product
		}
        [productReviewInstance: productReview]
    }

    def save() {
        def productReviewInstance = new ProductReview(params)
		productReviewInstance.postedDate = new Date()
		productReviewInstance.postedAnonymous = Boolean.FALSE
		
        if (!productReviewInstance.save(flush: true)) {
            render(view: "create", model: [productReviewInstance: productReviewInstance])
            return
        }

		Product product
		if(productReviewInstance.product) {
			product = productReviewInstance.product
			
			int reviewsCount = product?.productReviews?.size()
			
			def calculatedInfo = product.calculatedInfo
			if(!calculatedInfo) {
				calculatedInfo = new ProductCalculatedInfo()
				calculatedInfo.averageCustomerRating = productReviewInstance.overallRating
				calculatedInfo.averageQualityAndWorkmanshipRating = productReviewInstance.qualityAndWorkmanshipRating
				calculatedInfo.averageProductSatisfactionRating = productReviewInstance.productSatisfactionRating
				calculatedInfo.averageWowFactorRating = productReviewInstance.wowFactorRating
				
				calculatedInfo.save(flush:true)
			} else {
				int reviews = ProductReview.countByProduct(product)
				
				def c = ProductReview.createCriteria()
				BigDecimal overallRatingSum = c.get {
					projections {
						sum("overallRating")
					}
					like("product", product)
				};
				
				BigDecimal qualityAndWorkmanshipRatingSum = ProductReview.createCriteria().get {
					projections {
						sum("qualityAndWorkmanshipRating")
					}
					like("product", product)
				};
				
				BigDecimal productSatisfactionRatingSum = ProductReview.createCriteria().get {
					projections {
						sum("productSatisfactionRating")
					}
					like("product", product)
				};
				
				BigDecimal wowFactorRatingSum = ProductReview.createCriteria().get {
					projections {
						sum("wowFactorRating")
					}
					like("product", product)
				};
				
				calculatedInfo.averageCustomerRating = overallRatingSum/reviews
				calculatedInfo.averageQualityAndWorkmanshipRating = qualityAndWorkmanshipRatingSum/reviews
				calculatedInfo.averageProductSatisfactionRating = productSatisfactionRatingSum/reviews
				calculatedInfo.averageWowFactorRating = wowFactorRatingSum/reviews
				
				calculatedInfo.save(flush:true)
			}
			
			product.calculatedInfo = calculatedInfo
			product.save(flush:true)
		}
		
		redirect(controller:product.pdProductCategory,action: "viewDetails", id: product.pdProductId)
    }

    def show() {
        def productReviewInstance = ProductReview.get(params.id)
        if (!productReviewInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'productReview.label', default: 'ProductReview'), params.id])
            redirect(action: "list")
            return
        }

        [productReviewInstance: productReviewInstance]
    }

    def edit() {
        def productReviewInstance = ProductReview.get(params.id)
        if (!productReviewInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'productReview.label', default: 'ProductReview'), params.id])
            redirect(action: "list")
            return
        }

        [productReviewInstance: productReviewInstance]
    }

    def update() {
        def productReviewInstance = ProductReview.get(params.id)
        if (!productReviewInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'productReview.label', default: 'ProductReview'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (productReviewInstance.version > version) {
                productReviewInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'productReview.label', default: 'ProductReview')] as Object[],
                          "Another user has updated this ProductReview while you were editing")
                render(view: "edit", model: [productReviewInstance: productReviewInstance])
                return
            }
        }

        productReviewInstance.properties = params

        if (!productReviewInstance.save(flush: true)) {
            render(view: "edit", model: [productReviewInstance: productReviewInstance])
            return
        }

		flash.message = message(code: 'default.updated.message', args: [message(code: 'productReview.label', default: 'ProductReview'), productReviewInstance.productReviewId])
        redirect(action: "show", id: productReviewInstance.productReviewId)
    }

    def delete() {
        def productReviewInstance = ProductReview.get(params.id)
        if (!productReviewInstance) {
			flash.message = message(code: 'default.not.found.message', args: [message(code: 'productReview.label', default: 'ProductReview'), params.id])
            redirect(action: "list")
            return
        }

        try {
            productReviewInstance.delete(flush: true)
			flash.message = message(code: 'default.deleted.message', args: [message(code: 'productReview.label', default: 'ProductReview'), params.id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
			flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'productReview.label', default: 'ProductReview'), params.id])
            redirect(action: "show", id: params.id)
        }
    }
}
